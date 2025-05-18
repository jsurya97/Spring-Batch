package com.jk.spring_batch.processor;

import com.jk.spring_batch.model.Product;
import org.springframework.batch.item.ItemProcessor;

public class ProdutProcessor implements ItemProcessor<Product, Product> {

    @Override
    public Product process(Product product) throws Exception {
        if(product.getProductId()==2){
            return null;
        }else{
            product.setProductDesc(product.getProductDesc().toUpperCase());
        }
        return product;
    }
}
