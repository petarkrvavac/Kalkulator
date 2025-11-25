package ba.sum.fsre.kalkulator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvOperation: TextView

    private var currentNumber = ""
    private var expression = ""
    private var shouldResetDisplay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize display
        tvDisplay = findViewById(R.id.tvDisplay)
        tvOperation = findViewById(R.id.tvOperation)

        // Initial state - show 0 in result display (bottom)
        tvOperation.text = ""
        tvDisplay.text = "0"
        currentNumber = "0"

        // Number buttons
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { view ->
                onNumberClick((view as Button).text.toString())
            }
        }

        // Decimal point button
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            onDecimalClick()
        }

        // Operation buttons
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperationClick("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { onOperationClick("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperationClick("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperationClick("÷") }

        // Equals button
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEqualsClick() }

        // AC button - clear all
        findViewById<Button>(R.id.btnAC).setOnClickListener { onClearClick() }

        // DEL button - delete last digit
        findViewById<Button>(R.id.btnDel).setOnClickListener { onDeleteClick() }
    }

    private fun onNumberClick(number: String) {
        if (shouldResetDisplay || currentNumber == "0") {
            currentNumber = number
            shouldResetDisplay = false
        } else {
            currentNumber += number
        }
        tvDisplay.text = currentNumber
    }

    private fun onDecimalClick() {
        if (shouldResetDisplay) {
            currentNumber = "0."
            shouldResetDisplay = false
        } else if (!currentNumber.contains(".")) {
            currentNumber += "."
        }
        tvDisplay.text = currentNumber
    }

    private fun onOperationClick(operation: String) {
        if (currentNumber.isEmpty() || currentNumber == "0") return

        // Add current number to expression if not already added
        if (!shouldResetDisplay) {
            expression += currentNumber
        }

        expression += " $operation "
        tvOperation.text = expression

        shouldResetDisplay = true
    }

    private fun onEqualsClick() {
        if (expression.isEmpty()) return

        // Add current number to complete the expression
        val fullExpression = expression + currentNumber

        try {
            val result = evaluateExpression(fullExpression)

            // Show complete equation in top TextView
            tvOperation.text = "$fullExpression ="

            // Show result in bottom TextView
            tvDisplay.text = formatNumber(result)
            currentNumber = formatNumber(result)

            // Reset for next calculation
            expression = ""
            shouldResetDisplay = true

        } catch (e: Exception) {
            tvDisplay.text = "Error"
            tvOperation.text = ""
            expression = ""
            currentNumber = "0"
        }
    }

    private fun evaluateExpression(expr: String): Double {
        // Split expression by spaces to get tokens
        val tokens = expr.trim().split("\\s+".toRegex())

        if (tokens.isEmpty()) return 0.0

        // Convert to list for easier manipulation
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<String>()

        // Parse expression
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            when {
                token in listOf("+", "-", "×", "÷") -> {
                    operators.add(token)
                }
                else -> {
                    numbers.add(token.toDouble())
                }
            }
            i++
        }

        // First pass: handle × and ÷ (multiplication and division)
        i = 0
        while (i < operators.size) {
            when (operators[i]) {
                "×" -> {
                    val result = numbers[i] * numbers[i + 1]
                    numbers[i] = result
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                "÷" -> {
                    if (numbers[i + 1] == 0.0) {
                        throw ArithmeticException("Division by zero")
                    }
                    val result = numbers[i] / numbers[i + 1]
                    numbers[i] = result
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                else -> i++
            }
        }

        // Second pass: handle + and - (addition and subtraction)
        var result = numbers[0]
        for (j in operators.indices) {
            when (operators[j]) {
                "+" -> result += numbers[j + 1]
                "-" -> result -= numbers[j + 1]
            }
        }

        return result
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private fun onClearClick() {
        currentNumber = "0"
        expression = ""
        shouldResetDisplay = false
        tvOperation.text = ""
        tvDisplay.text = "0"
    }

    private fun onDeleteClick() {
        // Delete from expression if it exists
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1).trimEnd()
            tvOperation.text = expression

            // If expression becomes empty, reset to initial state
            if (expression.isEmpty()) {
                currentNumber = "0"
                tvDisplay.text = "0"
            }
        }
        // If expression is empty, delete from current number
        else if (currentNumber.length > 1) {
            currentNumber = currentNumber.dropLast(1)
            tvDisplay.text = currentNumber
        } else {
            currentNumber = "0"
            tvDisplay.text = "0"
        }
    }
}
