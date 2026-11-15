package com.example.chesshelper

import android.content.Context
import java.io.*
import java.util.Locale

/** Stockfish как внешний процесс (UCI). Бинарник: jniLibs/arm64-v8a/libstockfish.so */
class Engine(ctx: Context) {
    private val proc: Process =
        ProcessBuilder(ctx.applicationInfo.nativeLibraryDir + "/libstockfish.so").redirectErrorStream(true).start()
    private val rd = BufferedReader(InputStreamReader(proc.inputStream))
    private val wr = OutputStreamWriter(proc.outputStream)

    init {
        send("uci"); send("setoption name Threads value 2"); send("setoption name Hash value 64"); send("isready")
    }

    private fun send(s: String) { wr.write(s + "\n"); wr.flush() }

    /** Возвращает (ход вида e2e4, оценка). Оценка с точки зрения стороны, которая ходит. */
    @Synchronized
    fun best(fen: String, ms: Int = 2000): Pair<String, String> {
        send("position fen $fen"); send("go movetime $ms")
        var ev = ""
        while (true) {
            val l = rd.readLine() ?: return "" to ""
            if (l.startsWith("info") && " pv " in l) {
                Regex("score (cp|mate) (-?\\d+)").find(l)?.let { m ->
                    val v = m.groupValues[2].toInt()
                    ev = if (m.groupValues[1] == "cp") String.format(Locale.US, "%+.2f", v / 100.0) else "#$v"
                }
            }
            if (l.startsWith("bestmove")) return l.split(" ")[1] to ev
        }
    }

    fun close() {
        try { send("quit") } catch (e: Exception) { }
        proc.destroy()
    }
}
