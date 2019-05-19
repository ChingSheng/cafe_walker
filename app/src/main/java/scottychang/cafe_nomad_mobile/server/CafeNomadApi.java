package scottychang.cafe_nomad_mobile.server;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import scottychang.cafe_nomad_mobile.model.CoffeeShop;

import java.util.List;

public interface CafeNomadApi {
    @GET("{city}")
    Call<List<CoffeeShop>> getCoffeeShops(@Path("city") String city);
}
