package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ProductLineTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ProductRepository;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductLineTraitMapper implements TraitMapper<ProductLineTrait> {
    private final ProductRepository productRepository;

    @Override
    public void mapRequired(GeneralizedDoc doc, ProductLineTrait target, ProductLineTrait existingDoc) {
        Optional.ofNullable(doc.getInsurance_product())
                .flatMap(productRepository::findByCode)
                .ifPresent(product -> {
                    target.setProductLine(product.getLine());
                    target.setProductName(product.getProductName());
                });
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof ProductLineTrait;
    }
}
