using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace CatServer
{
    static class TcpCatServer
    {
        public static string CAT_URI;

        public static void Start(int port)
        {
            IPAddress ipAddress = IPAddress.Parse("127.0.0.1");

            Console.WriteLine("Starting TCP listener...");

            TcpListener listener = new TcpListener(ipAddress, port);
            listener.Start();

            while (true)
            {
                using (TcpClient client = listener.AcceptTcpClient())
                {
                    try
                    {
                        receiveData(client);
                    }
                    catch (IOException e)
                    {
                        Console.WriteLine(e);
                    }
                }

            }
        }

        private static void receiveData(TcpClient client)
        {
            List<string> headers = new List<string>();

            using (NetworkStream ns = client.GetStream())
            {
                StreamReader sr = new StreamReader(ns);
            
                while (true)
                {
                    string msg = sr.ReadLine();
                    if (String.IsNullOrEmpty(msg))
                        break;

                    if(msg.StartsWith("Host"))
                    {
                        msg = "Host: " + CAT_URI;
                    }

                    //F*ck gzip
                    if(msg.StartsWith("Accept-Encoding"))
                    {
                        headers.Add("Accept-Encoding: gzip");
                        continue;
                    }
                    
                    headers.Add(msg);
                }

                TcpCatClient catClient = new TcpCatClient(headers);

                if(catClient.Header.Count() != 0)
                    sendData(ns, catClient);
            }
        }

        private static void sendData(NetworkStream ns, TcpCatClient client)
        {
            //Body
            Byte[] bodyData = Encoding.UTF8.GetBytes(client.CatData);
            if (client.Header.ContainsKey("Content-Encoding") && client.Header["Content-Encoding"] == "gzip")
            {
                bodyData = Gzip.Compress(bodyData);
            }
            
            Console.WriteLine("- Send to client: " + bodyData.Length);

            //Add headers
            StringBuilder headers = new StringBuilder(client.HTTPStatus + "\r\n");
            foreach (KeyValuePair<string, string> pair in client.Header)
            {
                string value = pair.Value;

                if (pair.Key == "Content-Length")
                    value = bodyData.Length.ToString();

                headers.AppendFormat("{0}:{1}\r\n", pair.Key, value);
            }
            headers.Append("\r\n");

            Byte[] headerData = Encoding.UTF8.GetBytes(headers.ToString());
            ns.Write(headerData, 0, headerData.Length);

            //Add body
            if (bodyData.Length != 0)
            {
                ns.Write(bodyData, 0, bodyData.Length);
            }
            
            ns.Flush();
        }
    }
}
