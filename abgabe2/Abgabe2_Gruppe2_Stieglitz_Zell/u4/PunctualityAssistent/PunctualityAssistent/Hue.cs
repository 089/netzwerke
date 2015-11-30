using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PunctualityAssistent
{
    class Hue
    {
        struct JsonHue
        {
            public int hue { get; set; }
            public bool on { get; set; }
        }

        private const int LIGHT = 1;

        public string ServerIP { get; private set; }
        public string ServerKey { get; private set; }

        private TcpCatClient client = new TcpCatClient();

        public Hue(string serverIp, string key)
        {
            ServerIP = serverIp;
            ServerKey = key;
        }

        /// <summary>
        /// Set the color of the light
        /// </summary>
        /// <param name="hue">65280 = red, 25500 = green, 12750 = yellow</param>
        public void SetLight(int hue)
        {
            string url = string.Format("http://{0}/api/{1}/lights/{2}/state", ServerIP, ServerKey, LIGHT);

            JsonHue body = new JsonHue()
            {
                hue = hue,
                on = true,
            };

            string bodyJson = JsonConvert.SerializeObject(body);

            client.Put(url, bodyJson);
        }
    }
}
