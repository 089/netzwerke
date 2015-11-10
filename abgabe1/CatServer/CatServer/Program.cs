using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CatServer
{
    class Program
    {
        static void Main(string[] args)
        {
            if(args.Length != 1)
            {
                Console.WriteLine("No argument for server!");
                Console.WriteLine("Press any key to continue .. ");
                Console.ReadKey();
                return;
            }

            TcpCatServer.CAT_URI = args[0];
            TcpCatServer.Start(8082);
        }
    }
}
