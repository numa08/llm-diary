package net.numa08.llmdiary.ui.diary.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.numa08.llmdiary.data.local.entity.DiaryEntry
import net.numa08.llmdiary.domain.repository.DiaryRepository
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    diaryRepository: DiaryRepository,
) : ViewModel() {

    private val diaryId: Long = savedStateHandle["diaryId"]!!

    val diary: StateFlow<DiaryEntry?> = diaryRepository.observeById(diaryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
