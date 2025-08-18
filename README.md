# Quizzly - Aplicativo de Quiz para Android


Um aplicativo de quiz moderno e interativo para Android, construído com as tecnologias mais recentes. O Quizzly oferece uma experiência de usuário fluida, com autenticação segura e dados em tempo real, tudo alimentado pelo Firebase.

## Funcionalidades

* **Autenticação Segura:** Sistema completo de login e registo com e-mail e senha via Firebase Authentication.
* **Quiz Dinâmico:** As perguntas são carregadas diretamente do Cloud Firestore, permitindo adicionar novos quizzes sem atualizar o app.
* **Ranking Global:** Um sistema de ranking que atualiza a pontuação total dos jogadores em tempo real.
* **Histórico Pessoal:** Cada utilizador pode ver o seu histórico de quizzes jogados, com pontuações e datas.
* **Interface Moderna:** Construído 100% com Jetpack Compose, seguindo as melhores práticas de design do Material 3.

## Tecnologias Utilizadas

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Backend e Banco de Dados:** [Firebase Authentication](https://firebase.google.com/docs/auth) e [Cloud Firestore](https://firebase.google.com/docs/firestore)
* **Navegação:** [Jetpack Navigation for Compose](https://developer.android.com/jetpack/compose/navigation)

## Como Executar o Projeto

Para executar este projeto localmente, siga estes passos:

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/EveAlamin/QuizApp.git](https://github.com/EveAlamin/QuizApp.git)
    ```
2.  **Abra no Android Studio:** Abra o projeto clonado no Android Studio.
3.  **Configure o Firebase:**
    * Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
    * Adicione um app Android com o nome do pacote `com.example.quizapp`.
    * Faça o download do ficheiro `google-services.json` e coloque-o na pasta `app/` do projeto.
    * Ative o **Authentication** (com E-mail/Senha) e o **Cloud Firestore** (no modo de teste).
4.  **Execute o App:** Sincronize o projeto e execute no emulador ou num dispositivo físico.


