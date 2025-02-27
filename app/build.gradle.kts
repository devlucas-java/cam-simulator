plugins {
    alias(libs.plugins.android.application)  // Aplica o plugin Android Application para criar um aplicativo Android
    alias(libs.plugins.kotlin.android)  // Aplica o plugin Kotlin para Android, necessário para usar Kotlin no projeto
}

android {
    namespace = "com.example.camarax"  // Define o namespace (identificador único) do projeto, usado em pacotes de código.
    compileSdk = 35  // Define o SDK de compilação para o projeto, usado para compilar o aplicativo (a versão mais recente que o projeto pode usar)

    defaultConfig {
        applicationId = "com.example.camarax"  // Define o ID único do aplicativo no Play Store (usado para identificar o app)
        minSdk = 27  // Define a versão mínima do SDK necessária para executar o aplicativo. O aplicativo só funcionará em dispositivos com SDK 27 ou superior
        targetSdk = 35  // Define o SDK que o aplicativo foi otimizado para, ajuda o sistema Android a gerenciar a compatibilidade com o aplicativo
        versionCode = 1  // Define o código da versão, usado pelo sistema para identificar se uma versão é mais nova do que outra
        versionName = "1.0"  // Define o nome da versão do aplicativo, geralmente exibido no sistema ou na Play Store

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"  // Define o runner de testes padrão para o Android, necessário para testes instrumentados
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // Define se o código deve ser ofuscado e minimizado na versão de release. Aqui, está desativado.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),  // Usa o arquivo padrão de configuração ProGuard para otimização
                "proguard-rules.pro"  // Usa um arquivo de regras customizado para ProGuard
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11  // Define que o código será compilado com Java 11
        targetCompatibility = JavaVersion.VERSION_11  // Define que o código será compatível com Java 11 durante a execução
    }

    kotlinOptions {
        jvmTarget = "11"  // Define a versão JVM (Java Virtual Machine) para Kotlin como 11
    }

    viewBinding {
        enable = true  // Habilita o ViewBinding, que facilita o acesso e a manipulação das views no layout
    }
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")  // Adiciona a biblioteca padrão do Kotlin

    // Material Design
    implementation("com.google.android.material:material:1.8.0")  // Adiciona o Material Design para componentes de interface, como botões e caixas de texto

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.2.0")  // Adiciona o núcleo do CameraX para interagir com a câmera
    implementation("androidx.camera:camera-camera2:1.2.0")  // Adiciona suporte ao Camera2 API para interação avançada com a câmera
    implementation("androidx.camera:camera-lifecycle:1.2.0")  // Integra o CameraX com o ciclo de vida da activity ou fragmento
    implementation("androidx.camera:camera-view:1.5.0-alpha06")  // Adiciona o PreviewView, que exibe o conteúdo da câmera na interface

    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.4.0")  // Adiciona compatibilidade com versões anteriores do Android, garantindo que os componentes de UI funcionem corretamente
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")  // Adiciona a biblioteca ConstraintLayout para layout flexível
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")  // Adiciona suporte ao ciclo de vida da aplicação com Kotlin
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")  // Adiciona ViewModel, um componente para gerenciar dados da UI de maneira eficiente e separada do ciclo de vida
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.0")  // Adiciona o ViewModel sem Kotlin extension, mais genérico

    // Adicionais para o aplicativo de webcam fake
    implementation("androidx.core:core-ktx:1.7.0")  // Adiciona a biblioteca core do Android com extensões Kotlin
    implementation("androidx.activity:activity-ktx:1.4.0")  // Adiciona extensões Kotlin para facilitar o uso de Activity
    implementation("androidx.media:media:1.4.3")  // Adiciona a biblioteca de mídia para manipulação de arquivos de mídia

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")  // Adiciona o JUnit para testes unitários no código
    androidTestImplementation("androidx.test.ext:junit:1.1.3")  // Adiciona o JUnit para testes instrumentados no Android
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")  // Adiciona o Espresso para testes de UI no Android
}
