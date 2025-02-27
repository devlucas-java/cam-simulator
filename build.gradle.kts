// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false  // Aplica o plugin Android Application, mas não o ativa neste arquivo de nível superior.
    alias(libs.plugins.kotlin.android) apply false  // Aplica o plugin Kotlin para Android, mas não o ativa neste arquivo de nível superior.
}

configurations.all {
    resolutionStrategy {
        // A resolução de dependências força a versão específica das bibliotecas mencionadas.
        force ("androidx.lifecycle:lifecycle-viewmodel:2.5.0")  // Força a versão 2.5.0 do ViewModel da biblioteca Lifecycle.
        force ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0")  // Força a versão 2.5.0 da versão KTX (com extensões Kotlin) do ViewModel da biblioteca Lifecycle.
    }
}
