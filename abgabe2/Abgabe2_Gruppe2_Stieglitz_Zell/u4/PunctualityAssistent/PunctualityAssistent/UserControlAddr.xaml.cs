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
    /// Interaction logic for UserControlAddr.xaml
    /// </summary>
    public partial class UserControlAddr : UserControl
    {
        public string Address { get; private set; }

        public UserControlAddr()
        {
            InitializeComponent();
        }

        private void input_TextChanged(object sender, TextChangedEventArgs e)
        {
            if (String.IsNullOrEmpty(Street.Text)
                || String.IsNullOrEmpty(City.Text)
                || String.IsNullOrEmpty(Region.Text))
                Address = String.Empty;
            else
                Address = Street.Text + ", " + City.Text + ", " + Region.Text;
        }
    }
}
