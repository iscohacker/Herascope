package uz.iskandarbek.geraskop

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.min
import kotlin.math.max

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null
    private lateinit var circleView: ImageView
    private var xPos = 0f
    private var yPos = 0f
    private var xCenter = 0f
    private var yCenter = 0f
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.oq)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrateButton: ImageView = findViewById(R.id.shot)
        vibrateButton.setOnClickListener {
            mediaPlayer.start()
            // Vibratsiyani boshlash
            vibratePhone()
        }

        // SensorManager va gravitatsiya sensorini sozlash
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        // circleView ni topish
        circleView = findViewById(R.id.circleView)

        // Ekranning markaziy nuqtasini aniqlash
        circleView.post {
            xCenter = (circleView.parent as ConstraintLayout).width / 2f - circleView.width / 2f
            yCenter = (circleView.parent as ConstraintLayout).height / 2f - circleView.height / 2f

            // Dastlabki pozitsiyani markazga o'rnatish
            xPos = xCenter
            yPos = yCenter

            updateCirclePosition(0f, 0f)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            val gravityX = event.values[0]
            val gravityY = event.values[1]

            // Gravitatsiya sensorining ma'lumotlari asosida pozitsiyani yangilash
            updateCirclePosition(gravityX, gravityY)
        }
    }

    private fun updateCirclePosition(gravityX: Float, gravityY: Float) {
        val layoutParams = circleView.layoutParams as ConstraintLayout.LayoutParams

        // Gravitatsiya bo'yicha view'ni markazga intilish kuchi bilan boshqarish
        val springForce = 0.05f
        xPos += -gravityX * 10 - (xPos - xCenter) * springForce
        yPos += gravityY * 10 - (yPos - yCenter) * springForce

        // Ekran chegaralarini hisobga olish
        val widthLimit = (circleView.parent as ConstraintLayout).width - circleView.width
        val heightLimit = (circleView.parent as ConstraintLayout).height - circleView.height

        // Yangi pozitsiyalarni hisoblash
        xPos = min(max(xPos, 0f), widthLimit.toFloat())
        yPos = min(max(yPos, 0f), heightLimit.toFloat())

        // Pozitsiyani yangilash
        layoutParams.leftMargin = xPos.toInt()
        layoutParams.topMargin = yPos.toInt()

        // View pozitsiyasini yangilash
        circleView.layoutParams = layoutParams
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Bu funksiya kerak bo'lmasa bo'ladi
    }

    override fun onResume() {
        super.onResume()
        // Sensor monitoringni boshlash
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        // Sensor monitoringni to'xtatish
        sensorManager.unregisterListener(this)
    }

    private fun vibratePhone() {
        // Vibratsiya kuchini belgilash
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // Android 8.0 (API level 26) va undan yuqori versiyalar uchun
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // Eski versiyalar uchun
                vibrator.vibrate(100) // 1000 millisekund
            }
        }
    }
}
