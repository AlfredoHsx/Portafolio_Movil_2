package com.example.marsphotos.di

import android.app.Application
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.MarsPhotosRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRepository(application: Application): MarsPhotosRepository {
        val application = (application as MarsPhotosApplication).container
        return application.marsPhotosRepository
    }
}

//Notas:
//@Module: Indica que esta clase contiene métodos que proporcionan dependencias (es decir, métodos de "provisión" que crean instancias de objetos).
//@InstallIn(SingletonComponent::class): Indica que las dependencias proporcionadas por este módulo deben ser almacenadas en el SingletonComponent de Hilt,
// lo que significa que las dependencias estarán disponibles durante_todo el ciclo de vida de la aplicación
//@Provides: Especifica que Hilt debe utilizar este método para proporcionar una instancia de la dependencia indicada (en este caso, MarsPhotosRepository).
//@Singleton: Hace que la instancia de MarsPhotosRepository sea única en la aplicación, es decir, Hilt creará una sola instancia de este repositorio para toda la aplicación.
