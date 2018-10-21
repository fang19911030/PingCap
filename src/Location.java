public class Location {
    private String Province;
    private String City;

    public Location(String theProvince, String theCity) {
        Province = theProvince;
        City = theCity;
    }

    public Location(String theCity){
        Province = null;
        City = theCity;
    }

    public String getCity() {
        return City;
    }

    public String getProvince() {
        return Province;
    }

    @Override
    public String toString() {
        return Province+" "+City;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof  Location) &&
                ((Location)((Location) obj)).Province.equals(Province) &&
                ((Location)((Location) obj)).City.equals(City);

    }
}
