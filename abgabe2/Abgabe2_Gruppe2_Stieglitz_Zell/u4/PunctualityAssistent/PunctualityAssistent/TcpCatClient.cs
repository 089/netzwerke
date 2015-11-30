using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace PunctualityAssistent
{
    class TcpCatClient : IDisposable
    {
        private int PORT = 80;

        private TcpClient _client;
        private List<string> _requestHeaders;

        public Dictionary<string, string> Header { get; set; }
        public String HTTPStatus { get; set; }

        public string Data { get; private set; }
        
        public TcpCatClient(int port = 80)
        {
            PORT = port;

            _requestHeaders = new List<string>();
            //_requestHeaders.Add("Accept-Encoding: gzip");
            _requestHeaders.Add("Connection: close");
            //_requestHeaders.Add("Accept: text/html,application/xhtml+xml,application/xml,application/json");
        }

        public string Get(string url)
        {
            return send(url, "GET");
        }

        public string Put(string url, string data)
        {
            return send(url, "PUT", data);
        }

        private string send(string url, string type, string body = "")
        {
            Byte[] bodyData = Encoding.UTF8.GetBytes(body);

            Uri uri = new Uri(url);

            Console.WriteLine("Request: " + url);
            List<string> RHeader = new List<string>();

            RHeader.Add(type + " " + uri.PathAndQuery + " HTTP/1.1");
            RHeader.Add("Host: " + uri.Host);
            RHeader.AddRange(_requestHeaders);

            if(!String.IsNullOrEmpty(body))
                RHeader.Add("Content-Length: " + bodyData.Length);

            Header = new Dictionary<string, string>();
            
            if (!String.IsNullOrEmpty(url))
            {
                _client = new TcpClient(uri.DnsSafeHost, PORT);
                sendRequestHeader(RHeader);

                if (!String.IsNullOrEmpty(body))
                    sendRequestBody(bodyData);

                _client.GetStream().Flush();
                readData();
                return Data;
            }
            else
                return String.Empty;
        }


        private void sendRequestHeader(IList<string> header)
        {
            string message = string.Join("\r\n", header) + "\r\n\r\n";
            Byte[] data = Encoding.UTF8.GetBytes(message);
            NetworkStream stream = _client.GetStream();

            stream.Write(data, 0, data.Length);
        }
        
        private void sendRequestBody(Byte[] bodyData)
        {
            NetworkStream stream = _client.GetStream();
            stream.Write(bodyData, 0, bodyData.Length);
        }

        private void readData()
        {
            NetworkStream ns = _client.GetStream();
            ns.ReadTimeout = 1000;

            int bytes = 0;
            var buffer = new byte[4096];
            var hd = new MemoryStream();
            var response = new MemoryStream();
            var endHeader = false;

            do
            {
                try
                {
                    bytes = ns.Read(buffer, 0, buffer.Length);
                }
                catch (IOException)
                {
                    bytes = 0;
                    Console.WriteLine("- Debug: Timeout");
                }
                

                if (!endHeader)
                {
                    var startIndex = 0;
                    if (IsContainsHeaderCrLf(buffer, out startIndex))
                    {
                        endHeader = true;
                        hd.Write(buffer, 0, startIndex);
                        setHeader(hd);

                        response.Write(buffer, startIndex + 4, bytes - startIndex - 4);

                        if (Header.ContainsKey("Content-Length"))
                        {
                            Console.WriteLine("- Read with Content-Length");
                            int cl = int.Parse(Header["Content-Length"]);
                            cl -= bytes - startIndex - 4;
                            if(cl > 0)
                            {
                                //Bad for big files
                                byte[] buffer2 = new byte[cl];
                                ns.Read(buffer2, 0, cl);
                                response.Write(buffer2, 0, cl);
                            }

                            break;
                        }
                    }
                    else
                    {
                        hd.Write(buffer, 0, bytes);
                    }
                }
                else
                {
                    response.Write(buffer, 0, bytes);
                }

            } while (bytes != 0);
            
            byte[] responseBytes = response.ToArray();
            Console.WriteLine("- Response: " + responseBytes.Length);

            if (Header.ContainsKey("Content-Encoding") && Header["Content-Encoding"] == "gzip")
            {
                Console.WriteLine("- Decompress Gzip");
                byte[] tmp = Gzip.Decompress(responseBytes);
                Data = Encoding.UTF8.GetString(tmp);
            }
            else
            {
                Data = Encoding.UTF8.GetString(responseBytes);
            }
        }


        private void setHeader(MemoryStream headerStream)
        {
            string headertxt = Encoding.UTF8.GetString(headerStream.ToArray());
            string[] headerLines = Regex.Split(headertxt, "\r\n");

            foreach (string msg in headerLines)
            {
                if (String.IsNullOrEmpty(msg))
                    break;

                if (msg.StartsWith("HTTP"))
                {
                    HTTPStatus = msg;
                    Console.WriteLine("- Status: " + msg);
                    continue;
                }

                string[] parts = msg.Split(':');
                if (!Header.ContainsKey(parts[0]))
                    Header.Add(parts[0], parts[1].Trim());
            }
        }
        
        public void Dispose()
        {
            if (_client.Connected)
                _client.Close();
        }

        private bool IsContainsHeaderCrLf(byte[] buffer, out int startIndex)
        {
            for (var i = 0; i <= buffer.Length - 4; i++)
            {
                if (buffer[i] == 13 & buffer[i + 1] == 10 && buffer[i + 2] == 13 && buffer[i + 3] == 10)
                {
                    startIndex = i;
                    return true;
                }
            }
            startIndex = -1;
            return false;
        }
    }
}
