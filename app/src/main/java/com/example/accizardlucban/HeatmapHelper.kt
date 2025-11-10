package com.example.accizardlucban

import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource

object HeatmapHelper {

    fun ensureComponents(style: Style, sourceId: String, layerId: String) {
        ensureSource(style, sourceId)
        if (style.getLayer(layerId) == null) {
            style.addLayer(createHeatmapLayer(layerId, sourceId))
        }
    }

    private fun ensureSource(style: Style, sourceId: String) {
        if (style.getSource(sourceId) == null) {
            style.addSource(
                geoJsonSource(sourceId) {
                    featureCollection(FeatureCollection.fromFeatures(emptyList()))
                }
            )
        }
    }

    private fun createHeatmapLayer(layerId: String, sourceId: String) = heatmapLayer(layerId, sourceId) {
        heatmapColor(
            interpolate {
                linear()
                heatmapDensity()
                stop {
                    literal(0.0)
                    rgba(33.0, 102.0, 172.0, 0.0)
                }
                stop {
                    literal(0.2)
                    rgba(103.0, 169.0, 207.0, 0.6)
                }
                stop {
                    literal(0.4)
                    rgba(209.0, 229.0, 240.0, 0.8)
                }
                stop {
                    literal(0.6)
                    rgb(253.0, 219.0, 199.0)
                }
                stop {
                    literal(0.8)
                    rgb(239.0, 138.0, 98.0)
                }
                stop {
                    literal(1.0)
                    rgb(178.0, 24.0, 43.0)
                }
            }
        )
        heatmapIntensity(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(0.0)
                    literal(1.0)
                }
                stop {
                    literal(15.0)
                    literal(3.0)
                }
            }
        )
        heatmapRadius(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(0.0)
                    literal(15.0)
                }
                stop {
                    literal(14.0)
                    literal(40.0)
                }
            }
        )
        heatmapOpacity(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(11.0)
                    literal(0.8)
                }
                stop {
                    literal(16.0)
                    literal(0.3)
                }
            }
        )
        maxZoom(18.0)
    }

    fun updateFeatures(style: Style, sourceId: String, collection: FeatureCollection) {
        style.getSourceAs<GeoJsonSource>(sourceId)?.featureCollection(collection)
    }

    fun setVisibility(style: Style, layerId: String, visible: Boolean) {
        style.getLayer(layerId)?.visibility(if (visible) Visibility.VISIBLE else Visibility.NONE)
    }
}

