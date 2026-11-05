package com.apuntate.auth

import androidx.annotation.StringRes
import com.apuntate.R

/** Traduce un código de mensaje de autenticación a su recurso de texto. */
@StringRes
fun AuthMensaje.textoRes(): Int = when (this) {
    AuthMensaje.ERROR_REGISTRO -> R.string.auth_error_registro
    AuthMensaje.ERROR_LOGIN -> R.string.auth_error_login
    AuthMensaje.ERROR_EMAIL_RESET -> R.string.auth_error_email_reset
    AuthMensaje.RESET_ENVIADO -> R.string.auth_reset_enviado
    AuthMensaje.ERR_CREDENCIALES -> R.string.auth_err_credenciales
    AuthMensaje.ERR_NO_EXISTE -> R.string.auth_err_no_existe
    AuthMensaje.ERR_EMAIL_EN_USO -> R.string.auth_err_email_en_uso
    AuthMensaje.ERR_EMAIL_FORMATO -> R.string.auth_err_email_formato
    AuthMensaje.ERR_RED -> R.string.auth_err_red
    AuthMensaje.ERR_GENERICO -> R.string.auth_err_generico
    AuthMensaje.ERR_NOMBRE -> R.string.auth_err_nombre
}
