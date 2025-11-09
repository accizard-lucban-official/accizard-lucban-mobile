pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("Basic")
            }
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoiYWNjaXphcmQtbHVjYmFuIiwiYSI6ImNtY3ZvOTNtMTAwbGkya3Nicm42cHo4Y3AifQ.bSVJU_O9Z2wn7Gd6GXOX7Q"
            }
        }
    }
}

rootProject.name = "accizard-lucban"
include(":app")