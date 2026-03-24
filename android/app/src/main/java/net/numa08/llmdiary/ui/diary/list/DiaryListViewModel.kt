package net.numa08.llmdiary.ui.diary.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import net.numa08.llmdiary.data.local.entity.DiaryEntry
import net.numa08.llmdiary.domain.repository.DiaryRepository
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val diaries: StateFlow<List<DiaryEntry>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                diaryRepository.observeAll()
            } else {
                diaryRepository.searchByDate(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
