using PunctualityAssistent.GMaps;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace PunctualityAssistent
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
        }

        private void Window_Initialized(object sender, EventArgs e)
        {
            string time = DateTime.Now.AddHours(1).ToString();
            ArrivalTime.Text = time;
        }

        private void Send_Click(object sender, RoutedEventArgs e)
        {
            DateTime aTime;
            string dest = Target.Address;

            if(!DateTime.TryParse(ArrivalTime.Text, out aTime))
            {
                MessageBox.Show("Zeit wurde falsch angegeben");
                return;
            }

            if(String.IsNullOrEmpty(dest))
            {
                MessageBox.Show("Kein Zielort angegeben");
                return;
            }

            if(String.IsNullOrEmpty(tbKey.Text) || String.IsNullOrEmpty(tbServer.Text))
            {
                MessageBox.Show("Keine Config-Daten für HUE angegeben");
                return;
            }

            GoogleMaps maps = new GoogleMaps(dest);
            addWaypoint(maps, IntermediateTarget1);
            addWaypoint(maps, IntermediateTarget2);
            addWaypoint(maps, IntermediateTarget3);

            GoogleMapResult data = maps.RequestData();

            if(data == null || data.status == "NOT_FOUND")
            {
                MessageBox.Show("Ort wurde nicht gefunden!");
            }
            else if(data.status == "OK")
            {
                processImage(data);

                int totalSeconds = data.routes[0].legs.Sum(_ => _.duration.value);

                DateTime arrive = DateTime.Now.AddSeconds(totalSeconds);
                TimeSpan ts = aTime - arrive;
                InfoLabel.Text = "Restliche Minuten: " + Math.Round(ts.TotalMinutes) + "\n";
                InfoLabel.Text += "Ankunft: " + arrive.ToString();

                InfoLabel.Visibility = Visibility.Visible;

                Hue hueClient = new Hue(tbServer.Text, tbKey.Text);

                try {
                    if (ts.TotalMinutes > 15)
                    {
                        //Green
                        InfoLabel.Background = Brushes.Green;
                        hueClient.SetLight(25500);
                    }
                    else if (ts.TotalMinutes < 0)
                    {
                        //Red
                        InfoLabel.Background = Brushes.Red;
                        hueClient.SetLight(65280);
                    } else
                    {
                        //yellow
                        InfoLabel.Background = Brushes.Yellow;
                        hueClient.SetLight(12750);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Error connecting hue: " + ex.Message);
                }
            }
        }

        private void addWaypoint(GoogleMaps maps, UserControlAddr addr)
        {
            string address = addr.Address;
            if (!string.IsNullOrEmpty(address))
                maps.AddWaypoint(address);
        }

        private void processImage(GoogleMapResult result)
        {
            int width = 300;
            int height = (int)this.Height - (int)InfoLabel.Height - 20;

            StringBuilder gMapsImageRequest = new StringBuilder();
            gMapsImageRequest.Append("http://maps.google.com/maps/api/staticmap?");
            gMapsImageRequest.AppendFormat("size={0}x{1}", width, height);
            gMapsImageRequest.AppendFormat("&sensor=false&path=weight:3|color:red|enc:{0}", result.routes[0].overview_polyline.points);

            BitmapImage bitmap = new BitmapImage();
            bitmap.BeginInit();
            bitmap.UriSource = new Uri(gMapsImageRequest.ToString(), UriKind.Absolute);
            bitmap.EndInit();

            InfoImage.Height = height;
            InfoImage.Width = width;
            InfoImage.Source = bitmap;
        }

    }
}
