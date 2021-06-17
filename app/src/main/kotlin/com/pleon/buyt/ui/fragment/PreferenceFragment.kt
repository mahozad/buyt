package com.pleon.buyt.ui.fragment

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pleon.buyt.R
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.DB_NAME
import com.pleon.buyt.database.dao.DatabaseDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.isPremium
import com.pleon.buyt.serializer.*
import com.pleon.buyt.ui.activity.MainActivity
import kotlinx.android.synthetic.main.backup_data_widget_layout.*
import kotlinx.android.synthetic.main.export_data_widget_layout.*
import kotlinx.android.synthetic.main.restore_data_widget_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.BufferedWriter
import java.net.URI
import kotlin.system.exitProcess

const val PREF_LANG = "LANG"
const val PREF_THEME = "THEME"
const val PREF_EXPORT = "EXPORT"
const val PREF_BACKUP = "BACKUP"
const val PREF_RESTORE = "RESTORE"
const val PREF_VIBRATE = "VIBRATE"
const val PREF_LOG_SCALE = "LOG_SCALE"
const val PREF_SEARCH_DIST_DEF = "50"
const val PREF_SEARCH_DISTANCE = "DISTANCE"
const val PREF_FIRST_TIME_RUN = "FIRST_TIME_RUN"
const val PREF_TASK_RECREATED = "TASK_RECREATED"
const val PREF_THEME_AUTO = "auto"
const val PREF_THEME_DARK = "dark"
const val PREF_THEME_LIGHT = "light"

const val DEFAULT_THEME_NAME = PREF_THEME_DARK
const val DEFAULT_THEME_STYLE_RES = R.style.DarkTheme
private const val DEFAULT_EXPORT_FILE_NAME = "buyt-purchase-data"
private const val DEFAULT_BACKUP_FILE_NAME = "buyt-data"
private const val CREATE_EXPORT_REQUEST_CODE = 1
private const val CREATE_BACKUP_REQUEST_CODE = 2
private const val RESTORE_BACKUP_REQUEST_CODE = 3

@Suppress("unused")
class PreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    /**
     * Use this field wherever a context is needed to prevent exceptions.
     */
    private lateinit var activity: Activity
    private lateinit var serializers: List<Serializer<PurchaseDetail>>
    private lateinit var serializer: Serializer<PurchaseDetail>
    private val defaultSerializer = 0
    private val appDatabase: AppDatabase by inject()
    private val purchaseDao: PurchaseDao by inject()
    private val databaseDao: DatabaseDao by inject()
    private val contentResolver: ContentResolver by lazy {
        requireActivity().applicationContext.contentResolver
    }

    override fun onCreatePreferences(savedState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this)
        setupThemePreference()
        setupLanguagePreference()
        setupExportPreference()
        setupBackupPreference()
        setupRestorePreference()
    }

    private fun setupThemePreference() {
        findPreference<ListPreference>(PREF_THEME)?.apply {
            isEnabled = isPremium
            setTitle(if (isPremium) R.string.pref_title_theme else R.string.pref_title_theme_disabled)
        }
    }

    private fun setupLanguagePreference() {
        findPreference<ListPreference>(PREF_LANG)?.apply {
            isEnabled = isPremium
            setTitle(if (isPremium) R.string.pref_title_lang else R.string.pref_title_lang_disabled)
        }
    }

    private fun setupExportPreference() {
        val exportPreference = findPreference<Preference>(PREF_EXPORT)
        exportPreference?.apply {
            isEnabled = isPremium
            if (!isPremium) setSummary(R.string.pref_summary_export_disabled)
        }
        exportPreference?.setOnPreferenceClickListener {
            serializer = serializers[defaultSerializer] // Reset the serializer
            showExportPreferenceDialog()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setupBackupPreference() {
        val backupPreference = findPreference<Preference>(PREF_BACKUP)
        backupPreference?.apply {
            isEnabled = isPremium
            if (!isPremium) setSummary(R.string.pref_summary_backup_disabled)
        }
        backupPreference?.setOnPreferenceClickListener {
            val mimeType = "application/vnd.sqlite3"
            val fileName = "$DEFAULT_BACKUP_FILE_NAME.db"
            val requestCode = CREATE_BACKUP_REQUEST_CODE
            showSystemFileCreator(mimeType, fileName, requestCode)
            return@setOnPreferenceClickListener true
        }
    }

    private fun setupRestorePreference() {
        val restorePreference = findPreference<Preference>(PREF_RESTORE)
        restorePreference?.apply {
            isEnabled = isPremium
            if (!isPremium) setSummary(R.string.pref_summary_restore_disabled)
        }
        restorePreference?.setOnPreferenceClickListener {
            showSystemFilePicker(mimeType = "application/octet-stream")
            return@setOnPreferenceClickListener true
        }
    }

    private fun showExportPreferenceDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_select_export_format)
            .setNegativeButton(android.R.string.cancel) { _, _ -> /* Dismiss */ }
            .setPositiveButton(R.string.btn_text_select_export_location) { _, _ ->
                when (serializer) {
                    is PurchaseDetailsPDFSerializer -> createPDF()
                    else -> createOtherFormats()
                }
            }
            .setSingleChoiceItems(R.array.pref_export_names, defaultSerializer) { _, i ->
                serializer = serializers[i]
            }
            .create()
            .show()
    }

    private fun createPDF() = lifecycleScope.launch(Dispatchers.IO) {
        val purchaseDetails = purchaseDao.getAllPurchaseDetails()
        serializer.updateListener = { progress, _ ->
            withContext(Dispatchers.Main) { progressBar1?.setProgressCompat(progress, true) }
        }
        serializer.finishListener = { withContext(Dispatchers.Main) { progressBar1.hide() } }
        serializer.serialize(purchaseDetails)
    }

    private fun createOtherFormats() {
        val mimeType = serializer.mimeType
        val fileName = "$DEFAULT_EXPORT_FILE_NAME.${serializer.fileExtension}"
        val requestCode = CREATE_EXPORT_REQUEST_CODE
        showSystemFileCreator(mimeType, fileName, requestCode)
    }

    private fun showSystemFileCreator(mimeType: String, fileName: String, requestCode: Int) {
        val pickerInitialUri = URI("/")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = mimeType
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, fileName)
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, requestCode)
    }

    private fun showSystemFilePicker(mimeType: String) {
        val pickerInitialUri = URI("/")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(type))
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, RESTORE_BACKUP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        // The result data contains a URI for the document or directory that was selected
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CREATE_EXPORT_REQUEST_CODE -> exportData(resultData)
                CREATE_BACKUP_REQUEST_CODE -> createBackup(resultData)
                RESTORE_BACKUP_REQUEST_CODE -> restoreBackup(resultData)
            }
        }
    }

    private fun exportData(resultData: Intent?) {
        // This caused an exception in Android 5.1 and also was not necessary
        // progressBar.isIndeterminate = true
        progressBar1.show()
        resultData?.data?.also { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.bufferedWriter().use {
                    serialize(it!!)
                }
            }
        }
    }

    private fun createBackup(resultData: Intent?) {
        resultData?.data?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) { progressBar2.setProgressCompat(100, true) }
                val databaseFile = requireContext().getDatabasePath(DB_NAME)
                databaseDao.flushDatabase()
                contentResolver.openOutputStream(uri).use {
                    databaseFile.inputStream().copyTo(it!!)
                }
                delay(1000) // Show progress bar for a little while
                withContext(Dispatchers.Main) { progressBar2.hide() }
            }
        }
    }

    private fun restoreBackup(resultData: Intent?) {
        resultData?.data?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) { progressBar3.setProgressCompat(100, true) }
                appDatabase.close()
                val databaseFile = requireContext().getDatabasePath(DB_NAME)
                databaseFile.outputStream().use {
                    contentResolver.openInputStream(uri)?.copyTo(it)
                }
                withContext(Dispatchers.Main) { progressBar3.hide() }
                restartTheApp()
            }
        }
    }

    /**
     * FIXME: This may not work on Android Q and higher. See the following page:
     *  https://developer.android.com/guide/components/activities/background-starts
     */
    private fun restartTheApp() {
        val packageManager = requireContext().packageManager
        val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        requireContext().startActivity(mainIntent)
        exitProcess(0)
        // OR (with below method, by pressing back, goes to the previous app instance)
        /*val context = requireContext()
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity)
            context.finish()
        Runtime.getRuntime().exit(0)*/
    }

    private suspend fun serialize(writer: BufferedWriter) {
        val purchaseDetails = purchaseDao.getAllPurchaseDetails()
        serializer.updateListener = { progress, fragment ->
            writer.write(fragment)
            withContext(Dispatchers.Main) { progressBar1?.setProgressCompat(progress, true) }
        }
        serializer.finishListener = { withContext(Dispatchers.Main) { progressBar1.hide() } }
        serializer.serialize(purchaseDetails)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (key == PREF_THEME || key == PREF_LANG) {
            prefs.edit().putBoolean(PREF_TASK_RECREATED, true).apply()
            recreateTask()
        }
    }

    /**
     * Recreate the back stack so the new theme or language is applied to parent activities
     * (their onCreate() method is called which in turn invokes setTheme() or setLocale() method).
     * An alternative way would be to call setTheme() in onResume() callback of the main activity.
     *
     * Note: Use the [.activity] field initialized in [onAttach()][.onAttach]
     * as context to prevent exception when reset icon is pressed multiple times in a row.
     */
    private fun recreateTask() {
        TaskStackBuilder.create(activity)
                .addNextIntent(Intent(activity, MainActivity::class.java))
                .addNextIntent(activity.intent)
                .startActivities()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        val htmlSerializer = PurchaseDetailsHTMLSerializer(context)
        serializers = listOf(
            htmlSerializer,
            PurchaseDetailsPDFSerializer(context, DEFAULT_EXPORT_FILE_NAME, htmlSerializer),
            PurchaseDetailsCSVSerializer(),
            PurchaseDetailsJSONSerializer(),
            PurchaseDetailsXMLSerializer()
        )
        serializer = serializers[defaultSerializer]
    }
}
