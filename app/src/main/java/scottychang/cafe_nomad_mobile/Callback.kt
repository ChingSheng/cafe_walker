package scottychang.cafe_nomad_mobile

interface MyCallback<T> {
    fun onSuccess(result: T)
    fun onFailure(exception: Exception)
}