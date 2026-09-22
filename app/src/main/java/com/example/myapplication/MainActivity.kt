package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
)

data class QuizUiState(
    val currentQuestion: Int = 0,
    val selectedAnswer: Int? = null,
    val score: Int = 0,
    val quizFinished: Boolean = false,
)

class QuizViewModel : ViewModel() {

    private val questions = listOf(
        QuizQuestion(
            question = "Which language is primarily used for modern Android development?",
            options = listOf(
                "Kotlin",
                "Python",
                "C++",
                "HTML",
            ),
            correctAnswer = 0,
        ),
        QuizQuestion(
            question = "Which architecture is commonly used to separate UI and business logic?",
            options = listOf(
                "MVC",
                "MVVM",
                "OSI",
                "FIFO",
            ),
            correctAnswer = 1,
        ),
        QuizQuestion(
            question = "Which toolkit is used to build modern Android UI?",
            options = listOf(
                "Jetpack Compose",
                "Swing",
                "Tkinter",
                "JavaFX",
            ),
            correctAnswer = 0,
        ),
        QuizQuestion(
            question = "Which component stores UI-related data and survives configuration changes?",
            options = listOf(
                "ViewModel",
                "Intent",
                "Manifest",
                "Toast",
            ),
            correctAnswer = 0,
        ),
        QuizQuestion(
            question = "Which Kotlin feature is used for observable asynchronous state?",
            options = listOf(
                "StateFlow",
                "ArrayList",
                "Scanner",
                "String",
            ),
            correctAnswer = 0,
        ),
    )

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun selectAnswer(answerIndex: Int) {
        val currentState = _uiState.value

        if (currentState.selectedAnswer != null) {
            return
        }

        val question = questions[currentState.currentQuestion]
        val isCorrect = answerIndex == question.correctAnswer
        val newScore = if (isCorrect) {
            currentState.score + 1
        } else {
            currentState.score
        }

        _uiState.value = currentState.copy(
            selectedAnswer = answerIndex,
            score = newScore,
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value

        if (currentState.selectedAnswer == null) {
            return
        }

        if (currentState.currentQuestion == questions.lastIndex) {
            _uiState.value = currentState.copy(
                quizFinished = true,
            )
        } else {
            _uiState.value = currentState.copy(
                currentQuestion = currentState.currentQuestion + 1,
                selectedAnswer = null,
            )
        }
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState()
    }

    fun getCurrentQuestion(): QuizQuestion {
        return questions[_uiState.value.currentQuestion]
    }

    fun getTotalQuestions(): Int {
        return questions.size
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val quizViewModel: QuizViewModel = viewModel()
                val uiState = quizViewModel.uiState.collectAsStateWithLifecycle()

                QuizScreen(
                    uiState = uiState.value,
                    currentQuestion = quizViewModel.getCurrentQuestion(),
                    totalQuestions = quizViewModel.getTotalQuestions(),
                    onAnswerSelected = { answer ->
                        quizViewModel.selectAnswer(answer)
                    },
                    onNextClicked = {
                        quizViewModel.nextQuestion()
                    },
                    onRestartClicked = {
                        quizViewModel.restartQuiz()
                    },
                )
            }
        }
    }
}

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    currentQuestion: QuizQuestion,
    totalQuestions: Int,
    onAnswerSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    onRestartClicked: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        if (uiState.quizFinished) {
            ResultScreen(
                score = uiState.score,
                totalQuestions = totalQuestions,
                onRestartClicked = onRestartClicked,
                modifier = Modifier.padding(paddingValues),
            )
        } else {
            QuestionScreen(
                uiState = uiState,
                question = currentQuestion,
                totalQuestions = totalQuestions,
                onAnswerSelected = onAnswerSelected,
                onNextClicked = onNextClicked,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
fun QuestionScreen(
    uiState: QuizUiState,
    question: QuizQuestion,
    totalQuestions: Int,
    onAnswerSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Quiz Score Tracker",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        Text(
            text = "Question ${uiState.currentQuestion + 1} / $totalQuestions",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(
            modifier = Modifier.height(12.dp),
        )

        Text(
            text = "Running Score: ${uiState.score}",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(20.dp),
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        question.options.forEachIndexed { index, option ->
            AnswerOption(
                option = option,
                index = index,
                selectedAnswer = uiState.selectedAnswer,
                enabled = uiState.selectedAnswer == null,
                onSelected = {
                    onAnswerSelected(index)
                },
            )

            Spacer(
                modifier = Modifier.height(8.dp),
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        Button(
            onClick = onNextClicked,
            enabled = uiState.selectedAnswer != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (uiState.currentQuestion == (totalQuestions - 1)) {
                    "Finish Quiz"
                } else {
                    "Next Question"
                },
            )
        }
    }
}

@Composable
fun AnswerOption(
    option: String,
    index: Int,
    selectedAnswer: Int?,
    enabled: Boolean,
    onSelected: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selectedAnswer == index,
                onClick = onSelected,
                enabled = enabled,
            )

            Text(
                text = option,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    onRestartClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        Text(
            text = "Your Score",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(
            modifier = Modifier.height(10.dp),
        )

        Text(
            text = "$score / $totalQuestions",
            style = MaterialTheme.typography.displaySmall,
        )

        Spacer(
            modifier = Modifier.height(30.dp),
        )

        Button(
            onClick = onRestartClicked,
        ) {
            Text(
                text = "Restart Quiz",
            )
        }
    }
}