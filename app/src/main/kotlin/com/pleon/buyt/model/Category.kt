package com.pleon.buyt.model

import com.pleon.buyt.R
import com.pleon.buyt.viewmodel.StatsViewModel.Filter

/* TODO: store the enum as a separate table in the database.
 *  See [https://softwareengineering.stackexchange.com/a/305153/311271] */
enum class Category(val nameRes: Int,
                    val imageRes: Int,
                    val storeNameRes: Int,
                    val storeImageRes: Int) : Filter {

    GROCERY(R.string.item_cat_grocery, R.drawable.ic_item_grocery, R.string.store_cat_grocery_store, R.drawable.ic_item_grocery),
    BREAD(R.string.item_cat_bread, R.drawable.ic_item_bread, R.string.store_cat_bakery, R.drawable.ic_item_bread),
    DAIRY(R.string.item_cat_dairy, R.drawable.ic_item_dairy, R.string.store_cat_dairy, R.drawable.ic_item_dairy),
    FRUIT(R.string.item_cat_fruit, R.drawable.ic_item_fruit, R.string.store_cat_grocers, R.drawable.ic_item_fruit),
    MEAT(R.string.item_cat_meat, R.drawable.ic_item_meat, R.string.store_cat_butchery, R.drawable.ic_item_meat),
    CHICKEN(R.string.item_cat_chicken, R.drawable.ic_item_chicken, R.string.store_cat_chicken_shop, R.drawable.ic_item_chicken),
    FISH(R.string.item_cat_fish, R.drawable.ic_item_fish_shrimp, R.string.store_cat_fish_shop, R.drawable.ic_item_fish_shrimp),
    FAST_FOOD(R.string.item_cat_fast_food, R.drawable.ic_item_fast_food, R.string.store_cat_fast_food, R.drawable.ic_item_fast_food),
    ICE_CREAM(R.string.item_cat_ice_cream, R.drawable.ic_item_icecream_juice, R.string.store_cat_icecream_shop, R.drawable.ic_item_icecream_juice),
    PASTRY(R.string.item_cat_pastry, R.drawable.ic_item_pastry, R.string.store_cat_confectionery, R.drawable.ic_item_pastry),
    DRUG(R.string.item_cat_drug, R.drawable.ic_item_drug, R.string.store_cat_drugstore, R.drawable.ic_item_drug),
    BOOKSHOP(R.string.item_cat_book, R.drawable.ic_item_book, R.string.store_cat_book_shop, R.drawable.ic_item_book),
    HERBAL_TEA(R.string.item_cat_herbal_tea, R.drawable.ic_item_leaf, R.string.store_cat_herbal_tea_shop, R.drawable.ic_item_leaf),
    TOOLS(R.string.item_cat_tools, R.drawable.ic_item_tool, R.string.store_cat_hardware_store, R.drawable.ic_item_tool),
    FLOWER(R.string.item_cat_flower, R.drawable.ic_item_flower, R.string.store_cat_flower_shop, R.drawable.ic_item_flower),
    STATIONARY(R.string.item_cat_stationary, R.drawable.ic_item_stationary, R.string.store_cat_stationary_store, R.drawable.ic_item_stationary),
    OTHER(R.string.item_cat_other, R.drawable.ic_item_other, R.string.store_cat_other, R.drawable.ic_item_other);
    // SHOE_SHOP(R.string.store_cat_shoe_shop, R.drawable.ic_store_ice_creamy),
    // DRY_CLEANERS(R.string.store_cat_dry_cleaners, R.drawable.ic_store_ice_creamy),
    // CLOTHES_SHOP(R.string.store_cat_clothes_shop, R.drawable.ic_store_ice_creamy);

    override val criterion = name
    override val imgRes = imageRes
}
