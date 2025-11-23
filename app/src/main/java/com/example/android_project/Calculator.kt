package com.example.android_project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class CalculateButton(
    private val button: Button,
    private val mainText: TextView,
) {
    init {
        when (button.text) {
            "C" -> {
                button.setOnClickListener {
                    mainText.text = ""
                }
            }
            "⌫" -> {
                button.setOnClickListener {
                    val str = mainText.text.toString()
                    if (str.isNotEmpty()) {
                        mainText.text = str.substring(0, str.length - 1)
                    }
                }
            }
            "=" -> {
                button.setOnClickListener {
                    mainText.text = calculate(mainText.text.toString())
                }
            }
            "%" -> {
                button.setOnClickListener {
                    val currentText = mainText.text.toString()
                    if (currentText.isNotEmpty()) {
                        val number = currentText.toDouble()
                        val result = number / 100
                        mainText.text = if (result % 1 == 0.0) {
                            result.toInt().toString()
                        } else {
                            result.toString()
                        }

                    }
                }
            }
            "e" -> {
                button.setOnClickListener {
                    mainText.append("2.71828")
                }
            }
            else -> {
                button.setOnClickListener {
                    val textToAppend = if (button.text == ",") "." else button.text
                    mainText.append(textToAppend)
                }
            }
        }
    }
}

private fun calculate(expression: String): String {
    try {
        if (expression.isEmpty()) return ""

        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()
        var currentNumber = ""

        for (i in expression.indices) {
            val char = expression[i]

            if (char.isDigit() || char == '.') {
                currentNumber += char
            }

            if ((!char.isDigit() && char != '.') || i == expression.length - 1) {
                if (currentNumber.isNotEmpty()) {
                    numbers.add(currentNumber.toDouble())
                    currentNumber = ""
                }

                if (char == '+' || char == '-' || char == 'x' || char == '/') {
                    operators.add(char)
                }
            }
        }

        var i = 0
        while (i < operators.size) {
            val op = operators[i]
            if (op == 'x' || op == '/') {
                val a = numbers.getOrNull(i)
                val b = numbers.getOrNull(i + 1)
                if (a == null || b == null) {
                    return "Ошибочка"
                }
                val res = when (op) {
                    'x' -> a * b
                    '/' -> if (b != 0.0) a / b else return "Ошибочка деление на 0"
                    else -> 0.0
                }
                numbers[i] = res
                numbers.removeAt(i + 1)
                operators.removeAt(i)
            } else {
                i++
            }
        }

        var result = numbers.firstOrNull() ?: return "Ошибочка"
        for (j in operators.indices) {
            val op = operators[j]
            val b = numbers.getOrNull(j + 1) ?: continue
            when (op) {
                '+' -> result += b
                '-' -> result -= b
            }
        }

        return if (result % 1 == 0.0) {
            result.toInt().toString()
        } else {
            String.format("%.4f", result).removeSuffix("0").removeSuffix("0").removeSuffix(".")
        }

    } catch (e: Exception) {
        return "Ошибочка"
    }
}

class Calculator : AppCompatActivity() {
    private lateinit var mainText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)

        mainText = findViewById(R.id.CalculateText)

        listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
            R.id.button97, R.id.button96, R.id.button95, R.id.button94,
            R.id.button99, R.id.button91, R.id.button92, R.id.button93,
            R.id.button98, R.id.buttone
        ).forEach { id ->
            CalculateButton(findViewById(id), mainText)
        }
    }
}
//class Calculator : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_calculator)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}