package com.akl.templatemobile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Scale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import templatemobile.composeapp.generated.resources.Res
import templatemobile.composeapp.generated.resources.ic_google_pay
import templatemobile.composeapp.generated.resources.placeholder_db

/**
 * Clase que representa diferentes fuentes de recursos de imagen.
 *
 * Permite manejar imágenes provenientes de:
 * - Base64: Cadena en formato Base64 de la imagen.
 * - Bitmap: Objeto Bitmap de la imagen.
 * - URL: Dirección web de la imagen.
 * - assetFileName: Nombre del archivo en la carpeta assets.
 * - resourceId: ID del recurso drawable.
 *
 * @property base64 Cadena en formato Base64 de la imagen.
 * @property url URL de la imagen.
 * @property resourceId ID del recurso drawable.
 * @constructor Crea una instancia vacía de recursos de imagen.
 */
data class ImageResources(
    val url: String? = null,
    val resourceId: Int? = null,
    val drawableResource: DrawableResource? = null
)

/**
 * Componente personalizado para cargar imágenes de múltiples fuentes usando Coil.
 * Soporta imágenes desde recursos (drawable), assets, archivos, base64, bitmap y URLs.
 *
 * @param modifier Modifier para personalizar el layout
 * @param imageResources Clase que representa diferentes fuentes de recursos de imagen.
 * @param contentDescription Descripción del contenido para accesibilidad
 * @param contentScale Escala de contenido para la imagen
 * @param alpha Valor de transparencia
 * @param colorFilter Filtro de color opcional
 */
@Composable
fun AsyncImageComponent(
    modifier: Modifier = Modifier,
    imageResources: ImageResources,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val context = LocalPlatformContext.current
    val model = createImageRequest(
        drawableResource = imageResources.drawableResource,
        context = context,
        url = imageResources.url,
        resourceId = imageResources.resourceId
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            placeholder = painterResource(Res.drawable.placeholder_db),
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}

/**
 * Crea una solicitud de imagen para Coil basada en diferentes fuentes de datos.
 * La función prioriza las fuentes en el siguiente orden: base64 > bitmap > url > assetFileName > resourceId.
 *
 * @param context Contexto de Android necesario para acceder a recursos y strings
 * @param base64 String en formato Base64 de la imagen
 * @param url URL de la imagen a cargar
 * @param resourceId ID del recurso drawable
 * @return [ImageRequest] Configurada según la fuente de datos proporcionada
 */
private fun createImageRequest(
    context: PlatformContext,
    drawableResource: DrawableResource? = null,
    url: String? = null,
    resourceId: Int? = null
): ImageRequest {
    val data = when {
        drawableResource != null -> drawableResource
        url != null -> url
        resourceId != null -> resourceId
        else -> Res.drawable.ic_google_pay
    }
    return ImageRequest.Builder(context)
        .data(data)
        .crossfade(true)
        .precision(Precision.EXACT)
        .scale(Scale.FIT)
        .build()
}
