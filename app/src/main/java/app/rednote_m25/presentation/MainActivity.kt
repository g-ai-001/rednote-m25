package app.rednote_m25.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.rednote_m25.data.repository.AppLocale
import app.rednote_m25.data.repository.AppThemeMode
import app.rednote_m25.data.repository.UserPreferencesRepository
import app.rednote_m25.presentation.ui.category.CategoryScreen
import app.rednote_m25.presentation.ui.collection.CollectionScreen
import app.rednote_m25.presentation.ui.detail.NoteDetailScreen
import app.rednote_m25.presentation.ui.edit.EditNoteScreen
import app.rednote_m25.presentation.ui.explore.TopicExploreScreen
import app.rednote_m25.presentation.ui.home.HomeScreen
import app.rednote_m25.presentation.ui.profile.ProfileScreen
import app.rednote_m25.presentation.ui.publish.PublishScreen
import app.rednote_m25.presentation.ui.search.SearchScreen
import app.rednote_m25.presentation.ui.theme.RednoteTheme
import app.rednote_m25.util.LocaleManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var previousLocaleMode: AppLocale = AppLocale.SYSTEM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)
            val localeMode by userPreferencesRepository.localeMode.collectAsState(initial = AppLocale.SYSTEM)

            LaunchedEffect(localeMode) {
                if (previousLocaleMode != AppLocale.SYSTEM && localeMode != previousLocaleMode) {
                    previousLocaleMode = localeMode
                    recreate()
                }
                previousLocaleMode = localeMode
            }

            RednoteTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RednoteApp()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val localeMode = try {
            val prefs = newBase.getSharedPreferences("user_preferences", android.content.Context.MODE_PRIVATE)
            val localeName = prefs.getString("locale_mode", AppLocale.SYSTEM.name) ?: AppLocale.SYSTEM.name
            AppLocale.valueOf(localeName)
        } catch (e: Exception) {
            AppLocale.SYSTEM
        }
        super.attachBaseContext(LocaleManager.applyLocale(newBase, localeMode))
    }

@Composable
fun RednoteApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onCollectionClick = {
                    navController.navigate("collection")
                },
                onCategoryClick = {
                    navController.navigate("category")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }

        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                }
            )
        }

        composable("collection") {
            CollectionScreen(
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                }
            )
        }

        composable("category") {
            CategoryScreen(
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                },
                onExploreClick = {
                    navController.navigate("topic_explore")
                }
            )
        }

        composable("topic_explore") {
            TopicExploreScreen(
                onBackClick = { navController.popBackStack() },
                onTopicClick = { tag ->
                    navController.navigate("category?tag=$tag")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                },
                onPublishClick = {
                    navController.navigate("publish")
                }
            )
        }

        composable("publish") {
            PublishScreen(
                onBackClick = { navController.popBackStack() },
                onPublishSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "note_detail/{noteId}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) {
            NoteDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { noteId ->
                    navController.navigate("edit_note/$noteId")
                }
            )
        }

        composable(
            route = "edit_note/{noteId}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) {
            EditNoteScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}
