using Newtonsoft.Json;
using PunctualityAssistent.GMaps;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace PunctualityAssistent
{
    public class GoogleMaps
    {
        public const string URL = @"https://maps.googleapis.com/maps/api/directions/json?";

        private string origin;
        private string destination;

        private TcpCatClient client = new TcpCatClient();
        private IList<string> waypoints = new List<string>();
        
        public GoogleMaps(string dest)
        {
            if (String.IsNullOrEmpty(dest))
                throw new ArgumentNullException("No destination");

            origin = WebUtility.UrlEncode("Lothstraße 34, 80335 München, Germany");
            destination = WebUtility.UrlEncode(dest);
        }

        public void AddWaypoint(string waypoint)
        {
            if(!String.IsNullOrEmpty(waypoint))
            {
                waypoint = WebUtility.UrlEncode(waypoint);
                waypoints.Add(waypoint);
            }
        }

        public GoogleMapResult RequestData()
        {
            string url = URL + "origin=" + origin + "&destination=" + destination;

            string waypoints = string.Join("|", this.waypoints.ToArray());
            if (!String.IsNullOrEmpty(waypoints))
                url += "&waypoints=" + waypoints;

            
            string data = client.Get(url);
            var response = JsonConvert.DeserializeObject<GoogleMapResult>(data);
            return response;
        }
    }
}
