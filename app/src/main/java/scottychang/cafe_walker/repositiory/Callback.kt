package scottychang.cafe_walker.repositiory

interface MyCallback<T> {
    fun onSuccess(result: T)
    fun onFailure(exception: Exception)
}