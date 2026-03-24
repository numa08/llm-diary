package net.numa08.llmdiary.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.numa08.llmdiary.data.repository.DiaryRepositoryImpl
import net.numa08.llmdiary.data.repository.EventRepositoryImpl
import net.numa08.llmdiary.domain.repository.DiaryRepository
import net.numa08.llmdiary.domain.repository.EventRepository
import net.numa08.llmdiary.llm.LlamaCppEngine
import net.numa08.llmdiary.llm.LlmEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindLlmEngine(impl: LlamaCppEngine): LlmEngine
}
