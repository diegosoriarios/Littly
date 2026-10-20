package com.diego.littly.navigation

import androidx.lifecycle.ViewModel
import com.diego.littly.core.navigation.DeepLinkState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeepLinkViewModel @Inject constructor(
    val deepLinkState: DeepLinkState
) : ViewModel()
