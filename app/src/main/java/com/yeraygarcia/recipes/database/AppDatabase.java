package com.yeraygarcia.recipes.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yeraygarcia.recipes.database.dao.AisleDao;
import com.yeraygarcia.recipes.database.dao.IngredientDao;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.entity.Aisle;
import com.yeraygarcia.recipes.database.entity.Ingredient;
import com.yeraygarcia.recipes.database.entity.Placement;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.RecipeTag;
import com.yeraygarcia.recipes.database.entity.Store;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.Unit;

@Database(
        entities = {
                Aisle.class, Store.class, Tag.class, Unit.class, Ingredient.class, Placement.class,
                Recipe.class, RecipeIngredient.class, RecipeStep.class, RecipeTag.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "recipes";
    private static AppDatabase sInstance;
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(sInstance).execute();
                }
            };

    public static AppDatabase getDatabase(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .addCallback(sRoomDatabaseCallback)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }


    public abstract RecipeDao recipeDao();

    public abstract IngredientDao ingredientDao();

    public abstract AisleDao aisleDao();

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final RecipeDao mRecipeDao;
        private final IngredientDao mIngredientDao;
        private final AisleDao mAisleDao;

        PopulateDbAsync(AppDatabase db) {
            mRecipeDao = db.recipeDao();
            mIngredientDao = db.ingredientDao();
            mAisleDao = db.aisleDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mRecipeDao.deleteAll();

            populateAisle();
            populateIngredient();
            populateRecipe();

            return null;
        }

        private void populateAisle() {
            mAisleDao.insert(
                    new Aisle(1, "Nüsse & Trockenfrüchte"),
                    new Aisle(2, "Gemüse"),
                    new Aisle(3, "Obst"),
                    new Aisle(4, "Konserven"),
                    new Aisle(5, "Brotaufstrich"),
                    new Aisle(6, "Dressings"),
                    new Aisle(7, "Cerealien & Müsli"),
                    new Aisle(8, "Frische & Kühlung"),
                    new Aisle(9, "Brot"),
                    new Aisle(10, "Asiatisch"),
                    new Aisle(11, "Gewürze"),
                    new Aisle(12, "Pasta & Reis"),
                    new Aisle(13, "Backzutaten"),
                    new Aisle(14, "Fleisch und Fisch"),
                    new Aisle(15, "Kekse"),
                    new Aisle(16, "Haushalt"),
                    new Aisle(17, "Getränke"),
                    new Aisle(18, "Tiefkühl")
            );
        }

        private void populateIngredient() {
            mIngredientDao.insert(
                    new Ingredient(1, "Salz", 11),
                    new Ingredient(2, "Knoblauch", 2),
                    new Ingredient(3, "Pfeffer", 11),
                    new Ingredient(4, "Olivenöl", 6),
                    new Ingredient(5, "Zwiebeln", 2),
                    new Ingredient(6, "Butter", 8),
                    new Ingredient(7, "Gemüsebrühe", 11),
                    new Ingredient(8, "Eier", 8),
                    new Ingredient(9, "Kartoffeln", 2),
                    new Ingredient(10, "Zucker", 13),
                    new Ingredient(11, "Petersilie", 11),
                    new Ingredient(12, "Sahne", 8),
                    new Ingredient(13, "Tomaten", 2),
                    new Ingredient(14, "Mehl", 13),
                    new Ingredient(15, "Thymian", 11),
                    new Ingredient(16, "Milch", 8),
                    new Ingredient(17, "Paprika", 2),
                    new Ingredient(18, "Zucchini", 2),
                    new Ingredient(19, "Feta", 8),
                    new Ingredient(20, "Ingwer", 2),
                    new Ingredient(21, "Möhren", 2),
                    new Ingredient(22, "Oregano", 11),
                    new Ingredient(23, "Wasser", 17),
                    new Ingredient(24, "Zitronensaft", 3),
                    new Ingredient(25, "Champignons", 2),
                    new Ingredient(26, "Kokosmilch", 10),
                    new Ingredient(27, "Lauch", 2),
                    new Ingredient(28, "Paprika rot", 2),
                    new Ingredient(29, "Parmesan", 8),
                    new Ingredient(30, "Spaghetti", 12),
                    new Ingredient(31, "Bananen", 3),
                    new Ingredient(32, "Basilikum", 2),
                    new Ingredient(33, "Frühlingszwiebeln", 2),
                    new Ingredient(34, "Kichererbsen", 4),
                    new Ingredient(35, "Mozzarella", 8),
                    new Ingredient(36, "Muskat", 11),
                    new Ingredient(37, "Reis", 12),
                    new Ingredient(38, "Rosmarin", 11),
                    new Ingredient(39, "Thunfisch", 4),
                    new Ingredient(40, "Tomaten (Cherry)", 2),
                    new Ingredient(41, "Tomatensoße", 12),
                    new Ingredient(42, "Currypulver", 11),
                    new Ingredient(43, "Essig", 6),
                    new Ingredient(44, "Haferflocken", 7),
                    new Ingredient(45, "Hefe", 13),
                    new Ingredient(46, "Kokosöl", 10),
                    new Ingredient(47, "Koriander", 11),
                    new Ingredient(48, "Rucola", 2),
                    new Ingredient(49, "Schalotten", 2),
                    new Ingredient(50, "Walnüsse", 1),
                    new Ingredient(51, "Weißwein", 17),
                    new Ingredient(52, "Zitrone", 3),
                    new Ingredient(53, "Zwiebeln (rot)", 2),
                    new Ingredient(54, "Backpulver", 13),
                    new Ingredient(55, "Bacon", 14),
                    new Ingredient(56, "Basilikumblätter", 2),
                    new Ingredient(57, "Basmati-Reis", 12),
                    new Ingredient(58, "Blätterteig", 8),
                    new Ingredient(59, "Brokkoli", 2),
                    new Ingredient(60, "Dijon Senf", 6),
                    new Ingredient(61, "Dinkelmehl", 13),
                    new Ingredient(62, "Emmentaler/Gruyere/Parmesan (gerieben)", 8),
                    new Ingredient(63, "Fenchel", 2),
                    new Ingredient(64, "Frischkäse", 8),
                    new Ingredient(65, "Garnelen", 18),
                    new Ingredient(66, "Geschälte Tomaten", 2),
                    new Ingredient(67, "Grünkohl", 2),
                    new Ingredient(68, "Hackfleisch (Rind)", 14),
                    new Ingredient(69, "Hokkaido-Kürbis", 2),
                    new Ingredient(70, "Honig", 6),
                    new Ingredient(71, "Hähnchenkeulen", 14),
                    new Ingredient(72, "Kakao", 13),
                    new Ingredient(73, "Kapern", 11),
                    new Ingredient(74, "Kräuter der Provence", 11),
                    new Ingredient(75, "Kurkuma", 11),
                    new Ingredient(76, "Kölln Schoko Müsli Vorratspack", 7),
                    new Ingredient(77, "Lachs", 14),
                    new Ingredient(78, "Linsen", 4),
                    new Ingredient(79, "Maggi", 11),
                    new Ingredient(80, "Mandelblättchen", 1),
                    new Ingredient(81, "Nudeln", 12),
                    new Ingredient(82, "Paprikapulver", 11),
                    new Ingredient(83, "Sardellenfilet", 4),
                    new Ingredient(84, "Sojasauce", 10),
                    new Ingredient(85, "Sonnenblumenkerne", 1),
                    new Ingredient(86, "Spinat", 2),
                    new Ingredient(87, "Tomatenmark", 6),
                    new Ingredient(88, "Zimt", 11),
                    new Ingredient(89, "Actimel", 8),
                    new Ingredient(90, "Ahornsirup", 6),
                    new Ingredient(91, "Apfel (gerieben)", 3),
                    new Ingredient(92, "Apfel Braeburn", 3),
                    new Ingredient(93, "Asiatische Nudeln", 10),
                    new Ingredient(94, "Aubergine", 2),
                    new Ingredient(95, "Babyspinat", 2),
                    new Ingredient(96, "Balsamico-Creme", 6),
                    new Ingredient(97, "Balsamico-Essig", 6),
                    new Ingredient(98, "Berchtesgadener frische Milch 1,5%", 8),
                    new Ingredient(99, "Berchtesgadener Joghurt", 8),
                    new Ingredient(100, "Blattspinat", 2),
                    new Ingredient(101, "Bohnen", 4),
                    new Ingredient(102, "brauner Zucker", 13),
                    new Ingredient(103, "Brot", 9),
                    new Ingredient(104, "Butterflöckchen", 8),
                    new Ingredient(105, "Butterschmalz", 8),
                    new Ingredient(106, "Cashew-Nüsse", 1),
                    new Ingredient(107, "Cheddar", 8),
                    new Ingredient(108, "Chia-Samen", 1),
                    new Ingredient(109, "Chicorée", 2),
                    new Ingredient(110, "Ciabatta-Brötchen", 9),
                    new Ingredient(111, "Couscous", 12),
                    new Ingredient(112, "Cranberries", 1),
                    new Ingredient(113, "Cranberries (getrocknet)", 1),
                    new Ingredient(114, "Croutons", 9),
                    new Ingredient(115, "Datteln", 1),
                    new Ingredient(116, "Dinkel-Blätterteig", 8),
                    new Ingredient(117, "Eier (ganz frisch)", 8),
                    new Ingredient(118, "Eigelb", 8),
                    new Ingredient(119, "Emmentaler", 8),
                    new Ingredient(120, "Erdnussmus", 5),
                    new Ingredient(121, "Erdnüsse", 1),
                    new Ingredient(122, "Feldsalat", 2),
                    new Ingredient(124, "Fett", 8),
                    new Ingredient(125, "Feuchtes Toilettenpapier", 16),
                    new Ingredient(126, "frisch geriebene Muskatnuss", 11),
                    new Ingredient(127, "Fusilli", 12),
                    new Ingredient(128, "Garam Masala", 11),
                    new Ingredient(129, "gekochter Schinken", 8),
                    new Ingredient(130, "Gemüse", 2),
                    new Ingredient(131, "Geriebener Käse", 8),
                    new Ingredient(132, "Geschwärzte Oliven", 4),
                    new Ingredient(133, "Gewürzmischung „Kräuterbutter“", 11),
                    new Ingredient(134, "Gezuckerte Kondensmilch", 7),
                    new Ingredient(135, "Goji-Beeren", 1),
                    new Ingredient(136, "Gorgonzola", 8),
                    new Ingredient(137, "Granatapfel", 3),
                    new Ingredient(138, "Griechischer Joghurt", 8),
                    new Ingredient(139, "Grieß", 12),
                    new Ingredient(140, "Gruyere (12+ Monate)", 8),
                    new Ingredient(141, "Grünkohl (frisch)", 2),
                    new Ingredient(142, "Halbbitter-Kuvertüre", 13),
                    new Ingredient(143, "Haselnüsse (gemahlen)", 1),
                    new Ingredient(144, "Himbeere", 18),
                    new Ingredient(145, "Hohes C Orange Fruchtfleisch", 17),
                    new Ingredient(146, "Honigsenf", 6),
                    new Ingredient(147, "Italienische Kräuter", 11),
                    new Ingredient(148, "Kabeljau", 14),
                    new Ingredient(149, "Kaffee", 7),
                    new Ingredient(150, "kaltes Wasser", 17),
                    new Ingredient(151, "Kartoffeln (mehlig)", 2),
                    new Ingredient(152, "Kirschtomaten", 2),
                    new Ingredient(153, "Kiwi", 3),
                    new Ingredient(154, "Klopapier", 16),
                    new Ingredient(155, "Klärspüler", 16),
                    new Ingredient(156, "Kochschinken", 8),
                    new Ingredient(157, "Kokosflakes", 7),
                    new Ingredient(158, "Koriander (frisch)", 11),
                    new Ingredient(159, "Kumin (gemahlen)", 11),
                    new Ingredient(160, "Kuvertüre zartbitter", 13),
                    new Ingredient(161, "Käse", 8),
                    new Ingredient(162, "Käse (gerieben)", 8),
                    new Ingredient(163, "Küchenrollen", 16),
                    new Ingredient(164, "Kürbis", 2),
                    new Ingredient(165, "Laksapaste", 11),
                    new Ingredient(166, "Lasagneplatten", 12),
                    new Ingredient(167, "Laugengebäck", 9),
                    new Ingredient(168, "lauwarmes Wasser", 17),
                    new Ingredient(169, "Limettensaft", 3),
                    new Ingredient(170, "Linguiça-Wurst", 14),
                    new Ingredient(171, "Loorbeerblatt", 11),
                    new Ingredient(172, "Löffelbiskuits", 15),
                    new Ingredient(173, "Löslicher Kaffee", 7),
                    new Ingredient(174, "Maiskolben", 2),
                    new Ingredient(175, "Majoran", 11),
                    new Ingredient(176, "Makkaroni", 12),
                    new Ingredient(177, "Mandelmilch", 8),
                    new Ingredient(178, "Mandeln", 1),
                    new Ingredient(179, "Mandeln (gerieben)", 1),
                    new Ingredient(180, "Mangold", 2),
                    new Ingredient(181, "Marmelade (Aprikose)", 5),
                    new Ingredient(182, "Marzipan", 13),
                    new Ingredient(183, "Mediterrane Kräuter", 11),
                    new Ingredient(184, "Meersalz", 11),
                    new Ingredient(185, "Minze", 11),
                    new Ingredient(186, "mittelalter Gouda", 8),
                    new Ingredient(187, "Mohrrüben", 2),
                    new Ingredient(188, "Naturjoghurt", 8),
                    new Ingredient(189, "Nesquik", 7),
                    new Ingredient(190, "Oliven", 4),
                    new Ingredient(191, "Optional: Obst der Saison", 3),
                    new Ingredient(192, "Orange", 3),
                    new Ingredient(193, "Pappardelle", 12),
                    new Ingredient(194, "Paprika grün", 2),
                    new Ingredient(195, "Paprikapulver edelsüß", 11),
                    new Ingredient(196, "Pastinake", 2),
                    new Ingredient(197, "Pinienkerne", 1),
                    new Ingredient(198, "Pizzateig", 8),
                    new Ingredient(199, "Raclette-Käse", 8),
                    new Ingredient(200, "Reisessig", 10),
                    new Ingredient(201, "Ricotta", 8),
                    new Ingredient(202, "Risottoreis", 12),
                    new Ingredient(203, "Roggenmehl", 13),
                    new Ingredient(204, "Romana Salatherzen", 2),
                    new Ingredient(205, "Rosinen", 1),
                    new Ingredient(206, "Salami", 14),
                    new Ingredient(207, "Salat", 2),
                    new Ingredient(208, "Salatgurke", 2),
                    new Ingredient(209, "Salbeiblätter", 11),
                    new Ingredient(210, "Sambal Oelek", 10),
                    new Ingredient(211, "saure Sahne", 8),
                    new Ingredient(212, "Scamozza", 8),
                    new Ingredient(213, "Schnittlauch", 2),
                    new Ingredient(214, "Sellerie", 2),
                    new Ingredient(215, "Semmelbrösel", 13),
                    new Ingredient(216, "Sojasoße", 10),
                    new Ingredient(217, "Spargel", 2),
                    new Ingredient(218, "Speckschwarte", 14),
                    new Ingredient(219, "Speckwürfel", 14),
                    new Ingredient(220, "Spülmaschinensalz", 16),
                    new Ingredient(221, "Spülmaschinentabs", 16),
                    new Ingredient(222, "Suppengemüse", 2),
                    new Ingredient(223, "süße Sahne", 8),
                    new Ingredient(224, "süßer Senf", 6),
                    new Ingredient(225, "Süßkartoffeln", 2),
                    new Ingredient(226, "Tahini", 10),
                    new Ingredient(227, "Tempos", 16),
                    new Ingredient(228, "Tomate", 2),
                    new Ingredient(229, "Tomaten (getrocknet, in Öl)", 4),
                    new Ingredient(230, "Tomaten gewürfel", 2),
                    new Ingredient(231, "Vanillinzucker", 13),
                    new Ingredient(232, "Warmes wasser", 17),
                    new Ingredient(233, "Waschmittel", 16),
                    new Ingredient(234, "Weichspüler", 16),
                    new Ingredient(235, "Weinessig", 6),
                    new Ingredient(236, "Weizenmehl", 13),
                    new Ingredient(237, "Zahnpasta ", 16),
                    new Ingredient(238, "Ziegenfrischkäse", 8),
                    new Ingredient(239, "Zitronengras", 11)
            );
        }

        private void populateRecipe() {
            mRecipeDao.insert(
                    new Recipe(1, "Risotto (Claudio)", 4, 1800, null),
                    new Recipe(3, "Harvest Salad (Jamie Oliver)", 4, 4200, "https://youtu.be/KIhY4PSIl4w"),
                    new Recipe(5, "Spiegeleier mit Pommes", 2, 1500, null),
                    new Recipe(6, "Curry mit Hokkaido-Kürbis", 4, 1800, "Felix"),
                    new Recipe(7, "Nudeln mit Thunfisch, Feta und Tomaten", 2, 900, null),
                    new Recipe(8, "Spaghetti Bolognese", 4, 7200, "https://youtu.be/-gF8d-fitkU"),
                    new Recipe(9, "Potaje de lentejas", 4, 1800, "https://youtu.be/M9ujZE5uUm4"),
                    new Recipe(10, "Salmón marinado con ajo y jengibre", 4, 1800, null),
                    new Recipe(11, "Feldsalat mit Datteln und Mozzarella", 4, 1200, null),
                    new Recipe(12, "Mousse au chocolat", 4, 1800, null),
                    new Recipe(13, "Nudeln mit Salbeisoße", 4, 1200, "Felix"),
                    new Recipe(16, "Tzatziki", 4, 1800, "Felix"),
                    new Recipe(17, "Gemüsesuppe", 4, 1800, "Felix"),
                    new Recipe(18, "Müsli nach Mama", 4, 1800, "Felix"),
                    new Recipe(19, "Blätterteigschnecken", 4, 1800, "Felix"),
                    new Recipe(20, "Kartoffelgratin (Felix)", 4, 1800, "Renate"),
                    new Recipe(21, "Bratkartoffeln", 4, 1800, "Felix"),
                    new Recipe(22, "Nudelauflauf", 4, 1800, "Felix"),
                    new Recipe(23, "Brokkolisuppe", 4, 1800, "Felix"),
                    new Recipe(24, "Flammkuchen mit Zucchini und Feta", 4, 1800, "HelloFresh"),
                    new Recipe(25, "Fischfilet mit Zitronen-Kapern-Butter", 2, 1800, "Basic Cooking"),
                    new Recipe(26, "Herzhafter Linsen-Gemüse-Eintopf", 2, 1800, "https://www.hellofresh.de/recipes/herzhafter-linsen-gemuse-eintopf-n-58998670803b5070947df412"),
                    new Recipe(27, "Pizza", 2, 1800, null),
                    new Recipe(28, "Pappardelle in Gorgonzola-Soße", 4, 1800, "https://www.hellofresh.de/recipes/pappardelle-in-gorgonzola-spinatsose-n-58aac81f043c3c178617d5d3"),
                    new Recipe(29, "Mediterranen Gemüseauflauf mit Fenchel, Oliven und Ziegenkäse", 2, 1800, "https://www.hellofresh.de/recipes/mediterranen-gemuseauflauf-584fb4702e69d71ed752ee82"),
                    new Recipe(30, "Smoothie", 2, 900, null),
                    new Recipe(31, "Frühstück", 2, 60000, null),
                    new Recipe(32, "Pfannkuchen mit Lauchgemüse", 1, 1800, null),
                    new Recipe(33, "Caldo de papas", 4, 2400, "https://photos.app.goo.gl/XmUbCXl7AwYRKwfw2"),
                    new Recipe(34, "Couscous con espinacas y feta", 4, 1800, null),
                    new Recipe(35, "Tortilla de papas (Jamie Oliver)", 2, 2700, "https://youtu.be/JceGMNG7rpU"),
                    new Recipe(36, "Spaghetti al tonno", 4, 1800, "https://youtu.be/GdN2f65AHcU"),
                    new Recipe(37, "Kichererbsencurry", 2, 2100, "REWE-Magazin"),
                    new Recipe(38, "Grießbrei", 2, 600, null),
                    new Recipe(40, "Greek Lemon Chicken and Potatoes", 4, 3600, "https://youtu.be/h6OSMbfhIao"),
                    new Recipe(41, "Roast Potatoes (Jamie Oliver)", 4, 4200, "https://youtu.be/b1bXQlWLl7U"),
                    new Recipe(42, "Muslos de pollo en salsa", 4, 1500, "Mamá"),
                    new Recipe(43, "Porridge (Jamie Oliver)", 2, 600, null),
                    new Recipe(44, "Nudelauflauf (Oma Eich)", 4, 3600, "Oma Eich"),
                    new Recipe(45, "Empanadillas de atún", 4, 3600, null),
                    new Recipe(46, "Kichererbseneintopf", 4, 4200, "Mamá"),
                    new Recipe(47, "Spaghetti mit Zitronensahne und Garnelen", 4, 1800, "Basic Cooking S. 45"),
                    new Recipe(49, "Mac & Cheese (Jamie Oliver)", 4, 3600, "https://youtu.be/d6wI8SJWBXE"),
                    new Recipe(50, "Veggie Laksa", 4, 900, "https://www.evernote.com/shard/s19/res/53737d46-7acd-41c8-b60b-7092a42ba48e"),
                    new Recipe(51, "Reis mit Bohnen, Thunfisch, Feta und Tomaten", 2, 600, null),
                    new Recipe(52, "Hummus", 4, 1800, "https://youtu.be/WQlMXudBGT4"),
                    new Recipe(53, "Kürbissuppe mit Ingwer und Kokosmilch", 4, 2100, "http://www.chefkoch.de/rezepte/259781101566295/Kuerbissuppe-mit-Ingwer-und-Kokosmilch.html?portionen=6"),
                    new Recipe(54, "Caldo verde", 4, 5400, "https://youtu.be/MB5LAU5z9gk"),
                    new Recipe(55, "Pizza mit selbstgemachtem Teig", 4, 1800, null),
                    new Recipe(56, "Raclette", 4, 1800, null),
                    new Recipe(57, "Gefüllte Paprika mit Hackfleisch, Feta und Zucchini", 4, 1800, "https://www.chefkoch.de/rezepte/1509041256250767/Gefuellte-Paprika-mit-Hackfleisch-Feta-und-Zucchini.html"),
                    new Recipe(58, "Bremer Brot ", 4, 3600, "Geigenlehrerin Evers"),
                    new Recipe(59, "Kartoffelgratin (Renate)", 4, 5400, "Renate"),
                    new Recipe(60, "Schokokuchen", 12, 1800, "Simone Hahn (Balan)"),
                    new Recipe(61, "Süßkartoffel-Erdnuss-Suppe", 4, 3600, "https://www.chefkoch.de/rezepte/2826881434716907/Suesskartoffel-Erdnuss-Suppe.html"),
                    new Recipe(62, "Laugenbrötchen mit Scamozza", 4, 1800, "Simone Balan"),
                    new Recipe(63, "Schweizer Bürli", 6, 1800, "Eleni Balan"),
                    new Recipe(64, "Französisches Baguette", 4, 1800, "Eleni Balan"),
                    new Recipe(65, "Gratinierter Chicorée", 4, 2700, "GU Aufläufe"),
                    new Recipe(67, "Nudelauflauf mit Brokkoli", 4, 3900, "GU Aufläufe"),
                    new Recipe(69, "Lachs-Spinat-Lasagne", 4, 2100, "https://www.chefkoch.de/rezepte/247531098717236/Spinat-Lachs-Lasagne.html"),
                    new Recipe(70, "Vorrat", 1, 60000, null),
                    new Recipe(71, "Tarta de moca", 4, 3000, "https://photos.app.goo.gl/rheIL9sTB2RHgWT12"),
                    new Recipe(72, "Arroz a la cubana", 4, 1800, null),
                    new Recipe(73, "Nasi Goreng", 4, 2700, null),
                    new Recipe(74, "Salat", 2, 0, null),
                    new Recipe(76, "Bananen-Pancakes", 2, 600, "Runtasty"),
                    new Recipe(77, "Nussecken (Mama) ", 6, 0, "Mama (Lilo)")
            );
        }
    }

}
