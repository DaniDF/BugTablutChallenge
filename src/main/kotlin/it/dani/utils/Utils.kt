package it.dani.utils

import java.util.Random

class Utils {
    companion object {
        fun <T> getDistributedRandomElement(values: List<T>, probabilities: List<Double>): T {
            val distSum = probabilities.sumOf { prob -> prob }

            val rand = Math.random()
            val ratio: Double = 1.0f / distSum
            var tempDist = 0.0

            var result: T? = null

            probabilities.forEachIndexed { index, prob ->
                tempDist += prob

                if (rand / ratio <= tempDist) {
                    result = values[index]
                }
            }

            return result ?: values.first()
        }

        fun <T> getRandomElement(values : Collection<T>) : T {
            return values.toList()[Random().nextInt(values.size)]
        }
    }
}