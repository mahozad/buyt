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
import com.pleon.buyt.isPremium
import com.pleon.buyt.serializer.PurchaseDetailsCSVSerializer
import com.pleon.buyt.serializer.PurchaseDetailsHTMLSerializer
import com.pleon.buyt.serializer.PurchaseDetailsXMLSerializer
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
import java.io.FileWriter
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
private const val CREATE_EXPORT_FILE_REQUEST_CODE = 1
private const val CREATE_BACKUP_FILE_REQUEST_CODE = 2
private const val RESTORE_BACKUP_REQUEST_CODE = 3

@Suppress("unused")
class PreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    /**
     * Use this field wherever a context is needed to prevent exceptions.
     */
    private lateinit var activity: Activity
    private val serializers = listOf(PurchaseDetailsHTMLSerializer(), PurchaseDetailsXMLSerializer(), PurchaseDetailsCSVSerializer())
    private val defaultSerializer = 0
    private var serializer = serializers[defaultSerializer]
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
            if (!isPremium) setSummary(R.string.pref_title_export_disabled)
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
            /*if (!isPremium) setSummary(R.string.pref_title_backup_disabled)*/
        }
        backupPreference?.setOnPreferenceClickListener {
            showSystemFileSelectionForBackup()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setupRestorePreference() {
        val backupPreference = findPreference<Preference>(PREF_RESTORE)
        backupPreference?.apply {
            isEnabled = isPremium
            /*if (!isPremium) setSummary(R.string.pref_title_backup_disabled)*/
        }
        backupPreference?.setOnPreferenceClickListener {
            showSystemFileSelectionForRestore()
            return@setOnPreferenceClickListener true
        }
    }

    private fun showExportPreferenceDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_title_select_export_format)
                .setNegativeButton(android.R.string.cancel) { _, _ -> /* Dismiss */ }
                .setPositiveButton(R.string.btn_text_select_export_location) { _, _ ->
                    showSystemFileSelection()
                }
                .setSingleChoiceItems(R.array.pref_export_names, defaultSerializer) { _, i ->
                    serializer = serializers[i]
                }
                .create()
                .show()
    }

    private fun showSystemFileSelection() {
        val pickerInitialUri = URI("/")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = serializer.mimeType
            val fileName = "$DEFAULT_EXPORT_FILE_NAME.${serializer.fileExtension}"
            putExtra(Intent.EXTRA_TITLE, fileName)
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_EXPORT_FILE_REQUEST_CODE)
    }

    // FIXME: Duplicate of showSystemFileSelection()
    private fun showSystemFileSelectionForBackup() {
        val pickerInitialUri = URI("/")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.sqlite3"
            val fileName = "$DEFAULT_BACKUP_FILE_NAME.db"
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_BACKUP_FILE_REQUEST_CODE)
    }

    private fun showSystemFileSelectionForRestore() {
        val pickerInitialUri = URI("/")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(type))
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, RESTORE_BACKUP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == CREATE_EXPORT_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that the user selected.

            // This caused an exception in Android 5.1 and also was not necessary
            // progressBar.isIndeterminate = true
            progressBar1.show()
            resultData?.data?.also { uri ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val parcelDescriptor = contentResolver.openFileDescriptor(uri, "w")
                    val writer = FileWriter(parcelDescriptor!!.fileDescriptor).buffered()
                    writer.use { serialize(writer) }
                }
            }
        } else if (requestCode == CREATE_BACKUP_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { uri ->
                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) { progressBar2.setProgressCompat(100, true) }
                    databaseDao.flushDatabase()
                    val database = requireContext().getDatabasePath(DB_NAME)
                    contentResolver.openOutputStream(uri).use {
                        database.inputStream().copyTo(it!!)
                    }
                    delay(1000) // Show progress bar for a while
                    withContext(Dispatchers.Main) { progressBar2.hide() }
                }
            }
        } else if (requestCode == RESTORE_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { uri ->
                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) { progressBar3.setProgressCompat(100, true) }
                    appDatabase.close()
                    val database = requireContext().getDatabasePath(DB_NAME)
                    database.outputStream().use {
                        contentResolver.openInputStream(uri)?.copyTo(it)
                    }
                    // delay(1000) // Show progress bar for a while
                    withContext(Dispatchers.Main) { progressBar3.hide() }
                    restartTheApp()
                }
            }
        }
    }

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

    override fun onAttach(cxt: Context) {
        super.onAttach(cxt)
        this.activity = cxt as Activity
    }
}
