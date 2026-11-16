package com.miturno.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.miturno.app.R
import com.miturno.app.ui.theme.Blue50
import com.miturno.app.ui.theme.Blue700

/** Logotipo de Mi Turno: ticket de turno con personas al centro (recurso vectorial ic_logo). */
@Composable
fun MiTurnoLogo(modifier: Modifier = Modifier, width: Dp = 120.dp) {
    Image(
        painter = painterResource(R.drawable.ic_logo),
        contentDescription = null,
        modifier = modifier.size(width, width * 0.8333f)
    )
}

/** Icono sencillo de ticket para tarjetas de turnos. */
@Composable
fun TicketIcon(modifier: Modifier = Modifier, tint: Color = Blue700, size: Dp = 32.dp) {
    Icon(
        painter = painterResource(R.drawable.ic_ticket),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size)
    )
}

/** Forma del encabezado: borde inferior ondulado. */
object WaveHeaderShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.78f)
            cubicTo(w * 0.80f, h * 1.00f, w * 0.58f, h * 0.52f, w * 0.34f, h * 0.80f)
            cubicTo(w * 0.20f, h * 0.96f, w * 0.08f, h * 0.88f, 0f, h * 0.82f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Fondo de todas las pantallas: degradado blanco → azul muy claro con ondas suaves abajo. */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Blue50)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val back = Path().apply {
                moveTo(0f, h * 0.935f)
                cubicTo(w * 0.25f, h * 0.89f, w * 0.50f, h * 1.00f, w * 0.75f, h * 0.945f)
                cubicTo(w * 0.88f, h * 0.915f, w * 0.95f, h * 0.925f, w, h * 0.90f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(back, Color(0x334DA3FF))
            val front = Path().apply {
                moveTo(0f, h * 0.965f)
                cubicTo(w * 0.20f, h * 0.935f, w * 0.45f, h * 1.01f, w * 0.70f, h * 0.97f)
                cubicTo(w * 0.85f, h * 0.945f, w * 0.95f, h * 0.96f, w, h * 0.95f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(front, Color(0x444DA3FF))
        }
        content()
    }
}

/** Tarjeta principal con degradado azul (turno propio / turno en atención). */
@Composable
fun GradientCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Blue700, spotColor = Blue700)
            .background(Brush.linearGradient(listOf(Blue700, Color(0xFF3B8FE6))), shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}
