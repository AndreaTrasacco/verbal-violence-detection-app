package it.unipi.masss

/**
 * Intent actions to be used to START and STOP the recording service and to SEND ALERTS.
 */
enum class Action {
    START_RECORDING,
    STOP_RECORDING,
    START_SHAKING_DETECTION,
    STOP_SHAKING_DETECTION,
    SEND_ALERT
}