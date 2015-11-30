using System;

namespace PunctualityAssistent.GMaps
{
    public class GoogleMapResult
    {
        public routes[] routes { get; set; }
        public string status { get; set; }

    }

    public class routes
    {
        public bounds bounds { get; set; }
        public string copyrights { get; set; }
        public legs[] legs { get; set; }
        public overview_polyline overview_polyline { get; set; }
    }
    
    public class legs
    {
        public textValue distance { get; set; }
        public textValue duration { get; set; }
        public string start_address { get; set; }
        public geoaddr start_location { get; set; }
        public string end_address { get; set; }
        public geoaddr end_location { get; set; }
    }

    public class overview_polyline
    {
        public string points { get; set; }
    }

    public class textValue
    {
        public string text { get; set; }
        public int value { get; set; }
    }

    public class bounds
    {
        public geoaddr northeast { get; set; }
        public geoaddr southwest { get; set; }
    }

    public class geoaddr
    {
        public string lat { get; set; }
        public string lng { get; set; }
    }
}
