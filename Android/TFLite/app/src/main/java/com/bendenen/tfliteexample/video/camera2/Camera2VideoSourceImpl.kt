package com.bendenen.tfliteexample.video.camera2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.RenderScript
import android.util.Size
import com.bendenen.tfliteexample.video.VideoSourceListener
import com.bendenen.tfliteexample.video.VideoSource
import com.bendenen.tfliteexample.video.camera2.render.CameraRender
import com.bendenen.tfliteexample.video.camera2.render.RenderActionsListener
import com.bendenen.tfliteexample.video.camera2.render.rs.CustomRenderScriptCameraRender
import com.bendenen.tfliteexample.video.camera2.render.rs.IntrinsicRenderScriptCameraRender
import java.util.*
import java.util.concurrent.*

internal class Camera2VideoSourceImpl(
    private val application: Application,
    private val width: Int,
    private val height: Int
) : VideoSource, RenderActionsListener {
    private var videoSourceListener: VideoSourceListener? = null

    private lateinit var cameraRender: CameraRender
    private var useBitmap = false

    /** The [Size] of camera preview.  */
    private lateinit var previewSize: Size

    /** ID of the current [CameraDevice].  */
    private lateinit var cameraId: String

    /** A [CameraCaptureSession] for camera preview.  */
    private var captureSession: CameraCaptureSession? = null

    /** A [Semaphore] to prevent the app from exiting before closing the camera.  */
    private val cameraOpenCloseLock = Semaphore(1)

    /** A reference to the opened [CameraDevice].  */
    private var cameraDevice: CameraDevice? = null

    /** A [Handler] for running tasks in the background.  */
    private var backgroundHandler: Handler? = null

    /** An additional thread for running tasks that shouldn't block the UI.  */
    private var backgroundThread: HandlerThread? = null

    /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.  */
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(currentCameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraDevice = currentCameraDevice
            createCameraPreviewSession()
            cameraOpenCloseLock.release()
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            currentCameraDevice.close()
            cameraDevice = null
            cameraOpenCloseLock.release()
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            currentCameraDevice.close()
            cameraDevice = null
            cameraOpenCloseLock.release()
        }
    }

    /** A [CameraCaptureSession.CaptureCallback] that handles events related to capture.  */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
        }
    }


    /**
     * Orientation of the camera sensor
     */
    private var sensorOrientation = 0

    // Video source actions
    override fun getSourceWidth(): Int = previewSize.width

    override fun getSourceHeight(): Int = previewSize.height

    override fun isAttached(): Boolean = videoSourceListener != null

    override fun attach(videoSourceListener: VideoSourceListener) {
        this.videoSourceListener = videoSourceListener
        startBackgroundThread()
        openSource()
    }

    override fun detach() {
        closeSource()
        stopBackgroundThread()
        videoSourceListener = null
    }

    override fun release() {
        cameraRender.release()
    }

    override fun useBitmap(useBitmap: Boolean) {
        this.useBitmap = useBitmap
    }

    // Render actions
    override fun onNewRGBBytes(byteArray: ByteArray) {
        videoSourceListener?.onNewFrame(byteArray)
    }

    override fun onNewBitmap(bitmap: Bitmap) {
        videoSourceListener?.onNewBitmap(bitmap)
    }

    override fun useBitmap(): Boolean = useBitmap
    // end

    @SuppressLint("MissingPermission")
    private fun openSource() {
        setUpCameraOutputs()
        if(!::cameraRender.isInitialized) {
            cameraRender = CustomRenderScriptCameraRender(
                RenderScript.create(application),
                previewSize
            )
            cameraRender.renderActionsListener = this
        }

        val manager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    /** Closes the current [CameraDevice].  */
    private fun closeSource() {
        try {
            cameraOpenCloseLock.acquire()
            if (null != captureSession) {
                captureSession!!.close()
                captureSession = null
            }
            if (null != cameraDevice) {
                cameraDevice!!.close()
                cameraDevice = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /** Starts a background thread and its [Handler].  */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(HANDLE_THREAD_NAME)
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    /** Stops the background thread and its [Handler].  */
    private fun stopBackgroundThread() {
        backgroundThread!!.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    /** Creates a new [CameraCaptureSession] for camera preview.  */
    private fun createCameraPreviewSession() {
        try {

            cameraDevice?.let { camera ->

                val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(cameraRender.getSurface())
                }

                camera.createCaptureSession(
                    listOf(cameraRender.getSurface()),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            cameraOpenCloseLock.acquire()
                            // The camera is already closed
                            if (cameraDevice == null) {
                                cameraOpenCloseLock.release()
                                return
                            }
                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession

                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(
                                    CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                )

                                // Finally, we start displaying the camera preview.
                                cameraCaptureSession.setRepeatingRequest(
                                    previewRequestBuilder.build(),
                                    captureCallback,
                                    backgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            } finally {
                                cameraOpenCloseLock.release()
                            }
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            // TODO: Proceed edge case
                        }
                    },
                    null
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }


    /**
     * Sets up member variables related to camera.
     */
    private fun setUpCameraOutputs() {

        val manager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue

                previewSize = chooseOptimalSize(
                    choices = map.getOutputSizes(SurfaceTexture::class.java),
                    width = width,
                    height = height
                )

                /* Orientation of the camera sensor */
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

                this.cameraId = cameraId

                return
            }


        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            // TODO:Error handling
        }

    }

    companion object {

        private const val MINIMUM_PREVIEW_DIMENSION = 320

        private const val HANDLE_THREAD_NAME = "CameraBackground"


        /**
         * Given `choices` of `Size`s supported by a camera, chooses the smallest one whose
         * width and height are at least as large as the minimum of both, or an exact match if possible.
         *
         * @param choices The list of sizes that the camera supports for the intended output class
         * @param width   The minimum desired width
         * @param height  The minimum desired height
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        @JvmStatic
        private fun chooseOptimalSize(choices: Array<Size>, width: Int, height: Int): Size {
            val minDimension = Math.max(Math.min(width, height), MINIMUM_PREVIEW_DIMENSION)
            val desiredSize = Size(width, height)

            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            for (option in choices) {
                if (option == desiredSize) {
                    // Set the size but don't return yet so that remaining sizes will still be logged.
                    return desiredSize
                }

                if (option.height >= minDimension && option.width >= minDimension) {
                    bigEnough.add(option)
                }
            }

            // Pick the smallest of those, assuming we found any
            return if (bigEnough.size > 0) {
                Collections.min(bigEnough) { lhs: Size, rhs: Size ->
                    // We cast here to ensure the multiplications won't overflow
                    java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
                }
            } else {
                choices[0]
            }
        }
    }
}
