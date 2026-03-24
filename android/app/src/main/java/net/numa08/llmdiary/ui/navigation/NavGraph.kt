package net.numa08.llmdiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.numa08.llmdiary.ui.diary.detail.DiaryDetailScreen
import net.numa08.llmdiary.ui.diary.list.DiaryListScreen

object Routes {
    const val DIARY_LIST = "diary_list"
    const val DIARY_DETAIL = "diary_detail/{diaryId}"

    fun diaryDetail(diaryId: Long) = "diary_detail/$diaryId"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.DIARY_LIST) {
        composable(Routes.DIARY_LIST) {
            DiaryListScreen(
                onDiaryClick = { diaryId ->
                    navController.navigate(Routes.diaryDetail(diaryId))
                },
            )
        }
        composable(
            route = Routes.DIARY_DETAIL,
            arguments = listOf(navArgument("diaryId") { type = NavType.LongType }),
        ) {
            DiaryDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
