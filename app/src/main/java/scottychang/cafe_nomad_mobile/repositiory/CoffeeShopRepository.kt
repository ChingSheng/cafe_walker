package scottychang.cafe_nomad_mobile.repositiory

import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import scottychang.cafe_nomad_mobile.MyCallback
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.model.CoffeeShop
import scottychang.cafe_nomad_mobile.server.CafeNomadApi
import java.io.File
import java.io.FileReader
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit




class CoffeeShopRepository(private val context: Context) {
    private val url = "https://cafenomad.tw/api/v1.2/cafes/"
    private val timeoutSecond = 30L
    private val service: CafeNomadApi

    companion object {
        @Volatile private var INSTANCE: CoffeeShopRepository? = null

        fun getInstance(context: Context): CoffeeShopRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: CoffeeShopRepository(context).also { INSTANCE = it }
        }
    }

    init {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            .connectTimeout(timeoutSecond, TimeUnit.SECONDS)
            .readTimeout(timeoutSecond, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        service = retrofit.create(CafeNomadApi::class.java)
    }

    fun loadCoffeeShops(city: String, callback: MyCallback<List<CoffeeShop>>?) {
        val file = File(getFilePath(city))
        if (file.exists() && (createdBelowOneHour(file) || !isNetworkAvailable())) {
            val reader = JsonReader(FileReader(getFilePath(city)))
            val type = object : TypeToken<ArrayList<CoffeeShop>>() {}.type
            val data = Gson().fromJson<List<CoffeeShop>>(reader, type)
            callback?.onSuccess(data)
        } else {
            val callable = service.getCoffeeShops(city)
            callable.enqueue(object : Callback<List<CoffeeShop>> {
                override fun onResponse(call: Call<List<CoffeeShop>>, response: Response<List<CoffeeShop>>) {
                    cacheByFile(city, response.body())
                    callback?.onSuccess(response.body() ?: ArrayList())
                }

                override fun onFailure(call: Call<List<CoffeeShop>>, t: Throwable) {
                    val error = if (t is UnknownHostException) Exception(context.getString(R.string.network_error)) else t as Exception
                    callback?.onFailure(error)
                }
            })
        }
    }

    private fun cacheByFile(city: String, data: List<CoffeeShop>?) {
        val file = File(getFilePath(city))
        file.createNewFile()
        file.outputStream().use {
            val s = Gson().toJson(data)
            it.write(s.toByteArray())
            it.close()
        }
    }

    private fun createdBelowOneHour(file: File) = ((System.currentTimeMillis() - file.lastModified()) / 1000 / 60) < 60

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getFilePath(fileName : String): String {
        return context.getFilesDir().getPath().toString() + "/" + fileName + ".json"
    }
}