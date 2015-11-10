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

namespace CatServer
{
    class TcpCatClient : IDisposable
    {
        const string CAT_IMAGE = @"http://img-9gag-fun.9cache.com/photo/aYwpA1m_700b.jpg";
        const int PORT = 80;

        private TcpClient client;
        private List<string> requestHeaders;

        public Dictionary<string, string> Header { get; set; }
        public String HTTPStatus { get; set; }

        public string Data { get; private set; }

        public string CatData
        {
            get
            {
                string data = Regex.Replace(Data, "<img(.*?)src=\"(.*?)\"(.*?)>", "<img $1 src=\"" + CAT_IMAGE + "\" $3>");
                data = data.Replace("you", "you, admirer of cats and all things feline :-)");

                return data;
            }
        }


        public TcpCatClient(List<string> headers)
        {
            this.requestHeaders = headers;

            string get = headers.Where(_ => _.StartsWith("GET")).FirstOrDefault();
            Console.WriteLine("Request: " + get);

            Header = new Dictionary<string, string>();

            if (!String.IsNullOrEmpty(get))
            {
                client = new TcpClient(TcpCatServer.CAT_URI, PORT);
                sendRequestHeader();
                readData();
            }
        }

        private void readData()
        {
            NetworkStream ns = client.GetStream();
            ns.ReadTimeout = 500;

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

        private void sendRequestHeader()
        {
            string message = string.Join("\r\n", requestHeaders) + "\r\n\r\n";
            Byte[] data = System.Text.Encoding.UTF8.GetBytes(message);
            NetworkStream stream = client.GetStream();

            stream.Write(data, 0, data.Length);
            stream.Flush();
        }

        public void Dispose()
        {
            if (client.Connected)
                client.Close();
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
