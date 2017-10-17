package com.TpFinal.data.dao;

import com.TpFinal.data.dao.interfaces.DAOCobro;
import com.TpFinal.data.dto.cobro.Cobro;

public class DAOCobroImpl extends DAOImpl<Cobro> implements DAOCobro{

	public DAOCobroImpl() {
		super(Cobro.class);
	}

	
}