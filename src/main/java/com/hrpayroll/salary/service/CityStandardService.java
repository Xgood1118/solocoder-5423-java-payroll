package com.hrpayroll.salary.service;

import com.hrpayroll.common.BusinessException;
import com.hrpayroll.salary.entity.CityStandard;
import com.hrpayroll.salary.repository.CityStandardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CityStandardService {

    @Autowired
    private CityStandardRepository cityStandardRepository;

    public CityStandard create(CityStandard standard) {
        if (cityStandardRepository.findByCityCodeAndYear(
                standard.getCityCode(), standard.getYear()).isPresent()) {
            throw new BusinessException("该城市该年度标准已存在");
        }
        validateStandard(standard);
        standard.setEnabled(true);
        return cityStandardRepository.save(standard);
    }

    public CityStandard update(String id, CityStandard standard) {
        CityStandard existing = cityStandardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("城市标准不存在"));
        validateStandard(standard);
        existing.setCityName(standard.getCityName());
        existing.setSocialSecurityMinBase(standard.getSocialSecurityMinBase());
        existing.setSocialSecurityMaxBase(standard.getSocialSecurityMaxBase());
        existing.setSocialSecurityPersonalRate(standard.getSocialSecurityPersonalRate());
        existing.setSocialSecurityCompanyRate(standard.getSocialSecurityCompanyRate());
        existing.setYear(standard.getYear());
        existing.setEnabled(standard.isEnabled());
        return cityStandardRepository.save(existing);
    }

    public void delete(String id) {
        if (!cityStandardRepository.existsById(id)) {
            throw new BusinessException("城市标准不存在");
        }
        cityStandardRepository.deleteById(id);
    }

    public CityStandard getById(String id) {
        return cityStandardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("城市标准不存在"));
    }

    public CityStandard getByCityAndYear(String cityCode, int year) {
        return cityStandardRepository.findByCityCodeAndYear(cityCode, year)
                .orElseThrow(() -> new BusinessException("未找到城市社保标准: " + cityCode + "/" + year));
    }

    public List<CityStandard> listByYear(int year) {
        return cityStandardRepository.findByYear(year);
    }

    public List<CityStandard> listAll() {
        return cityStandardRepository.findAll();
    }

    private void validateStandard(CityStandard standard) {
        if (standard.getSocialSecurityMinBase().compareTo(standard.getSocialSecurityMaxBase()) > 0) {
            throw new BusinessException("最低基数不能大于最高基数");
        }
        if (standard.getSocialSecurityPersonalRate().compareTo(BigDecimal.ZERO) < 0
                || standard.getSocialSecurityPersonalRate().compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException("个人缴费比例必须在 0 到 1 之间");
        }
    }
}
