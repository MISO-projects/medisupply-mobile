package com.medisupply.ui.viewmodels

sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
    object NavigateToClientHome : NavigationEvent() 
}
