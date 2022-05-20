package ecommerce.service.order.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import ecommerce.service.client.base.Order;

@Mapper  
public interface OrderDAOFromMySQL {

	@Select({"<script>",
		"SELECT * FROM orders",
		"WHERE 1=1",

		"<when test='order.parentId!=null'>",
		"AND parent_id = #{order.parentId}",
		"</when>",
		"<when test='order.userId!=null'>",
		"AND user_id = #{order.userId}",
		"</when>",
		"<when test='order.productId!=null'>",
		"AND product_id = #{order.productId}",
		"</when>",
		"<when test='order.status!=null'>",
		"AND status = #{order.status}",
		"</when>",

		"ORDER BY id,user_id,product_id,parent_id,status LIMIT #{start}, #{size}",
		"</script>"})
	@Results(id="orderResults", value={
			@Result(property="id",   column="id"),
			@Result(property="parentId",  column="parent_id"),
			@Result(property="userId", column="user_id"),
			@Result(property="productId", column="product_id"),
			@Result(property="receiptAmount", column="receipt_amount"),
			@Result(property="paidAmount", column="paid_amount"),
			@Result(property="status", column="status"),
			@Result(property="createTime", column="create_time"),
	})
	public List<Order> queryOrders(Order order, int start, int size);

	@Select("SELECT * FROM orders WHERE id = #{id} LIMIT 1")
	@ResultMap("orderResults")
	public Order getOrderById(long id);
	
	@Select("SELECT * FROM orders WHERE id = #{id} AND user_id=#{id} LIMIT 1")
	@ResultMap("orderResults")
	public Order getOrderByIdAndUserId(long id, long userId);
	
	@Insert("INSERT INTO orders(id,parent_id,user_id,product_id,receipt_amount,create_time) SELECT #{id}, #{parentId}, #{userId}, #{productId}, #{receiptAmount}, #{createTime}"
			+ " FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM orders WHERE id = #{id} LIMIT 1)")
	@Results(id="insert", value={
			@Result(property="id", column="id", javaType = Boolean.class),
		})
	public boolean insert(Order order);
	
	@Update("UPDATE orders SET status=#{status} WHERE id=#{order.id} AND status=#{order.status} LIMIT 1")
	@Results(id="updateStatus", value={
			@Result(property="id", column="id", javaType = Boolean.class),
		})
	public boolean updateStatus(Order order, int status);

	@Update("UPDATE orders SET paid_amount=paid_amount+#{paidAmount} WHERE id=#{id} LIMIT 1")
	@Results(id="increPaidAmount", value={
			@Result(property="id", column="id", javaType = Boolean.class),
		})
	public boolean increPaidAmount(long id, long paidAmount);
	
//	@Delete("DELETE FROM orders WHERE parent_id=#{id} or id =#{id} LIMIT 1")
//	public int delete(Long id);

}
