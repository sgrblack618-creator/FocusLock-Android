package com.focuslock.dns.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.dns.security.PinSecurityManager
import com.focuslock.dns.ui.strings.AppStrings
import com.focuslock.dns.ui.theme.FocusLockColors

@Composable
fun VerifyAdminPinDialog(
    strings: AppStrings,
    onVerify: (String) -> Boolean,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FocusLockColors.Surface,
        title = { Text(text = strings.pinVerifyTitle, color = FocusLockColors.TextPrimary) },
        text = {
            Column {
                Text(text = strings.pinVerifySubtitle, color = FocusLockColors.TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 8 && it.all { ch -> ch.isDigit() }) {
                            pinInput = it
                            errorText = null
                        }
                    },
                    label = { Text(strings.pinInputLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = FocusLockColors.DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerify(pinInput)) {
                        onSuccess(pinInput)
                    } else {
                        errorText = strings.pinWrongError
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusLockColors.FocusEmerald)
            ) {
                Text(strings.confirmBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn, color = FocusLockColors.TextSecondary)
            }
        }
    )
}

@Composable
fun SetAdminPinDialog(
    strings: AppStrings,
    onSavePin: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FocusLockColors.Surface,
        title = { Text(strings.pinSetupTitle, color = FocusLockColors.TextPrimary) },
        text = {
            Column {
                Text(
                    text = strings.pinSetupDesc,
                    color = FocusLockColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 8 && it.all { ch -> ch.isDigit() }) {
                            pin = it
                            errorText = null
                        }
                    },
                    label = { Text(strings.pinNewLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 8 && it.all { ch -> ch.isDigit() }) {
                            confirmPin = it
                            errorText = null
                        }
                    },
                    label = { Text(strings.pinConfirmLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = FocusLockColors.DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        !PinSecurityManager.isValidPinFormat(pin) -> {
                            errorText = strings.pinFormatError
                        }
                        pin != confirmPin -> {
                            errorText = strings.pinMismatchError
                        }
                        onSavePin(pin) -> onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusLockColors.FocusEmerald)
            ) {
                Text(strings.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn, color = FocusLockColors.TextSecondary)
            }
        }
    )
}

@Composable
fun CustomDurationDialog(
    strings: AppStrings,
    initialDays: Int,
    initialHours: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var daysText by remember { mutableStateOf(initialDays.toString()) }
    var hoursText by remember { mutableStateOf(initialHours.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FocusLockColors.Surface,
        title = { Text(strings.customDialogTitle, color = FocusLockColors.TextPrimary) },
        text = {
            Column {
                Text(
                    text = strings.customDialogDesc,
                    color = FocusLockColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) daysText = it },
                        label = { Text(strings.daysUnit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) hoursText = it },
                        label = { Text(strings.hoursRangeLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = FocusLockColors.DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = daysText.toIntOrNull() ?: 0
                    val h = hoursText.toIntOrNull() ?: 0
                    if (d == 0 && h == 0) {
                        errorText = strings.customDialogErrorMin
                    } else if (d > 365 || h > 23) {
                        errorText = strings.customDialogErrorMax
                    } else {
                        onConfirm(d, h)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusLockColors.FocusEmerald)
            ) {
                Text(strings.chooseBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn, color = FocusLockColors.TextSecondary)
            }
        }
    )
}
