   M S B A M L   `   `   `  ����O  KPunctualityAssistent, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null;"clr-namespace:PunctualityAssistentPunctualityAssistent  5   	   %    PunctualityAssistent.MainWindow   5   	   \ XPresentationFramework, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35W SPresentationCore, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35R NWindowsBase, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35D 9http://schemas.microsoft.com/winfx/2006/xaml/presentation    5   	   R NSystem.Xaml, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     5   	   @d2http://schemas.microsoft.com/expression/blend/2008     5   	   Jmc;http://schemas.openxmlformats.org/markup-compatibility/2006     5   	   4local"clr-namespace:PunctualityAssistent     5   	   -   5          � Title$  
MainWindow��5   	   $	��650��6   $	��700��6)   .��5	      ]� ��. .+�5
   
   �� -   5         ��Name$ 	InfoLabel��5
   ,   $��Bottom=�6   $��80��6?   $
��Bold"�6K   $��32(�5      $
��Wrap=�6    �� TextAlignment$ Center=�62   Restliche Zeit:6Z   �� 5   
   -   5         ��Name$ 	InfoImage��5   '   $��Right=�6   6'   �� 5   
   $
��Auto=�6   .��5      �� .+�5      �� $��Config��6   $
��Bold"�6)   � 5       � RowDefinitions 5      �� 5      $
��Auto �6(   �� 5      $
��Auto �6(    � ColumnDefinitions 5      �� 5      $	��120 �6+   �� 5      $��* �6+   .+�5      �� $��0��6   $��0��6)   .��69   Server6A   �� 5      $��1��6   $��0��6)   .��69   Key6>   �� 5      -   5         ��Name$ tbServer��5   ;   $��0��6   $��1��6+   $��3q�6M   6Z   �� 5       -   5        $ tbKey��5    ;   $��1��6   $��1��6+   $��3q�6J   6W   5!      �� 5#      $��	Fahrtziel��6   $
��Bold"�6,   *   $PunctualityAssistent.UserControlAddr  5$      -   5          Name$ Target��5$   (   �� 5&       �� Orientation$ 
Horizontal=�6   $
��Left=�66   .+�5'      �� $	��120��6   .��6(   	Ankunft61   �� 5(      -   5        $ ArrivalTime��5(      $	��150��63   5)      9� 5+      -   5        	 9�Name$	 expander��5+      $��Weitere Fahrtziele��6-   $��2��6I   $��0��6V   $��2��6f   
 9� 
IsExpanded
 .  .��5,      �� .+�5-      �� $
��Bold"�6    .��62   Fahrtziel 16?     5.      -	   5        $ IntermediateTarget1��5.   a   $��1��60   $��0��6=   $��2��6M   6a   �� 5/      $
��Bold"�6    .��62   Fahrtziel 26?     50      -
   5        $ IntermediateTarget2��50   a   $��1��60   $��0��6=   $��2��6M   6a   �� 51      $
��Bold"�6    .��62   Fahrtziel 36?     52      -   5        $ IntermediateTarget3��52   a   $��1��60   $��0��6=   $��2��6M   6a   53      54      �� 56      -   5        $��Right=�56      $��80��57      $��3q�6#   $
��Send��6.   6=   59      5:      5;      5<      