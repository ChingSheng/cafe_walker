package scottychang.cafe_nomad_mobile.model

enum class TwCity(var type: String) {
    UNKNOWN(""),
    TAIPEI("taipei"),
    KEELUNG("keelung"),
    TAOYUAN("taoyuan"),
    HSINCHU("hsinchu"),
    MIAOLI("miaoli"),
    TAICHUNG("taichung"),
    CHANGHUA("changhua"),
    NANTOU("nantou"),
    YUNLIN("yunlin"),
    CHIAYI("chiayi"),
    TAINAN("tainan"),
    KAOHSIUNG("kaohsiung"),
    PINGTUNG("pingtung"),
    YILAN("yilan"),
    HUALIEN("hualien"),
    TAITUNG("taitung"),
    PENGHU("penghu"),
    LIENCHIANG("lienchiang");

    override fun toString(): String {
        return type // working!
    }
}