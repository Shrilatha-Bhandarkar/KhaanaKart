package com.onlinefoodorder.util;

import com.onlinefoodorder.dto.DashboardStatsDto.ItemStat;
import com.onlinefoodorder.dto.DashboardStatsDto.UserStat;
import com.onlinefoodorder.dto.DashboardStatsDto.RestaurantStat;

import org.knowm.xchart.*;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Charts {

	private static final String OUTPUT_DIR = "charts/";

	public static byte[] getTopSellingItemsChartBytes(List<ItemStat> items) {
		PieChart chart = new PieChartBuilder().width(600).height(400).title("Top Selling Items").build();

		chart.getStyler().setLegendVisible(true);
		chart.getStyler().setPlotContentSize(0.95);
		chart.getStyler().setCircular(true);

		long total = items.stream().mapToLong(ItemStat::getQuantitySold).sum();
		for (ItemStat item : items) {
			long qty = item.getQuantitySold();
			double percentage = (qty * 100.0) / total;
			String label = String.format("%s (%.1f%%)", item.getItemName(), percentage);
			chart.addSeries(label, qty);
		}

		return toPngByteArray(chart);
	}

	public static byte[] getActiveUsersChartBytes(List<UserStat> users) {
		CategoryChart chart = new CategoryChartBuilder().width(600).height(400).title("Most Active Users")
				.xAxisTitle("User").yAxisTitle("Order Count").build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
		chart.getStyler().setAvailableSpaceFill(0.9);

		chart.addSeries("Orders", users.stream().map(UserStat::getUsername).toList(),
				users.stream().map(UserStat::getOrderCount).toList());

		return toPngByteArray(chart);
	}

	public static byte[] getRestaurantRevenueChartBytes(List<RestaurantStat> restaurants) {
		CategoryChart chart = new CategoryChartBuilder().width(600).height(400).title("Top Restaurants by Revenue")
				.xAxisTitle("Restaurant").yAxisTitle("Revenue").build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
		chart.getStyler().setAvailableSpaceFill(0.9);

		chart.addSeries("Revenue", restaurants.stream().map(RestaurantStat::getRestaurantName).toList(),
				restaurants.stream().map(r -> r.getValue().doubleValue()).toList());

		return toPngByteArray(chart);
	}

	private static byte[] toPngByteArray(org.knowm.xchart.internal.chartpart.Chart<?, ?> chart) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] generateDeliveryLineChart(String title, String xAxisTitle, String yAxisTitle, List<Date> dates,
			List<Long> orderCounts) {
		XYChart chart = new XYChartBuilder().width(600).height(400).title(title).xAxisTitle(xAxisTitle)
				.yAxisTitle(yAxisTitle).build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

		chart.addSeries("Orders Delivered", dates, orderCounts); // X: Date, Y: Long

		return toPngByteArray(chart);
	}

	public static byte[] generateTotalDeliveredChart(String title, String xAxisTitle, String yAxisTitle,
			List<String> names, List<Long> orderCounts) {
		CategoryChart chart = new CategoryChartBuilder().width(600).height(400).title(title).xAxisTitle(xAxisTitle)
				.yAxisTitle(yAxisTitle).build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
		chart.getStyler().setAvailableSpaceFill(0.9);

		chart.addSeries("Delivered Orders", names, orderCounts);

		return toPngByteArray(chart);
	}

	public static byte[] generateRestaurantBarChart(String title, String xAxisTitle, String yAxisTitle,
			List<String> itemNames, List<Long> quantities) {
		if (itemNames.isEmpty() || quantities.isEmpty()) {
			System.out.println("⚠️ Chart received empty data, using default values.");
			itemNames = List.of("No Data");
			quantities = List.of(0L);
		}

		CategoryChart chart = new CategoryChartBuilder().width(600).height(400).title(title).xAxisTitle(xAxisTitle)
				.yAxisTitle(yAxisTitle).build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
		chart.getStyler().setAvailableSpaceFill(0.9);

		chart.addSeries("Quantity Sold", itemNames, quantities);

		return toPngByteArray(chart);
	}

	public static byte[] getDeliveryPersonDailyChart(String title, String xAxisTitle, String yAxisTitle,
			List<String> deliveryPersons, List<Long> orderCounts) {
		CategoryChart chart = new CategoryChartBuilder().width(600).height(400).title(title).xAxisTitle(xAxisTitle)
				.yAxisTitle(yAxisTitle).build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
		chart.getStyler().setAvailableSpaceFill(0.9);
		chart.getStyler().setPlotGridVerticalLinesVisible(false);

		// Add the data
		chart.addSeries("Delivered Orders", deliveryPersons, orderCounts);

		return toPngByteArray(chart);
	}

}
