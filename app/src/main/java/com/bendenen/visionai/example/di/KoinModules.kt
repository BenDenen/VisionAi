package com.bendenen.visionai.example.di

import com.bendenen.visionai.example.repository.BlendModeRepository
import com.bendenen.visionai.example.repository.StyleRepository
import com.bendenen.visionai.example.screens.artisticstyletransfer.ArtisticStyleTransferActivity
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.AddNewStyleUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.ArtisticStyleTransferFunctionsUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.GetBlendModeListUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.GetStyleListUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.viewmodel.ArtisticStyleTransferViewModel
import com.bendenen.visionai.example.utils.VisionAiManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    single<StyleRepository> { StyleRepository.Impl() }
    single<BlendModeRepository> { BlendModeRepository.Impl() }
    single<VisionAiManager> { VisionAiManager.Impl(androidApplication()) }

    scope(named<ArtisticStyleTransferActivity>()) {
        scoped<AddNewStyleUseCase> { AddNewStyleUseCase.Impl(get()) }
        scoped<GetStyleListUseCase> { GetStyleListUseCase.Impl(get()) }
        scoped<ArtisticStyleTransferFunctionsUseCase> {
            ArtisticStyleTransferFunctionsUseCase.Impl(androidContext(), get())
        }
        scoped<GetBlendModeListUseCase> { GetBlendModeListUseCase.Impl(get()) }
        viewModel {
            ArtisticStyleTransferViewModel(get(), get(), get(), get())
        }
    }

}