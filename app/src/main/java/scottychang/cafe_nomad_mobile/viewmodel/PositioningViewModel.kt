package scottychang.cafe_nomad_mobile.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import scottychang.cafe_nomad_mobile.model.LatLng
import scottychang.cafe_nomad_mobile.repositiory.PositioningRepository

class PositioningViewModel(application: Application) : AndroidViewModel(application) {
    var latLng = MutableLiveData<LatLng>()
        private set

    init {
        reloadFromGps()
    }

    fun reloadFromGps() {
        val positioning = PositioningRepository.loadLatlng(getApplication())
        latLng.postValue(positioning)
    }
}