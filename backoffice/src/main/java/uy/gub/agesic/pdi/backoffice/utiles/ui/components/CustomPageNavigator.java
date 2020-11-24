package uy.gub.agesic.pdi.backoffice.utiles.ui.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import uy.gub.agesic.pdi.backoffice.config.BackofficeProperties;
import uy.gub.agesic.pdi.backoffice.utiles.spring.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class CustomPageNavigator<T> extends Panel {

	private static final long serialVersionUID = 1L;

	private List<Integer> PREDEFINED_SIZES;

	private Integer pageSize;
	private Integer currentPage;
	private Long totalRows;

	private Component anchor;
	private List<Object> pageIndexes = new ArrayList<>();
	
	public CustomPageNavigator(String id) {
		super(id);
		this.inicializar();
	}

	public CustomPageNavigator(String id, Component anchor) {
		super(id);
		this.anchor = anchor;
		this.inicializar();
	}
	
	private void inicializar() {
		// Tamanios predefinidos, los traemos del negocio porque pueden cambiar
		this.updatePredefinedSizes();
		
		// Esta lista contiene los indices generados segun los diferentes parametros del navegador
		this.pageIndexes = new ArrayList<>();

		DropDownChoice<Integer> cantidadesDrop = new PageSizeDropDownChoice();
		cantidadesDrop.setEscapeModelStrings(false);
		cantidadesDrop.setNullValid(false);
		cantidadesDrop.setVersioned(false);

		this.add(cantidadesDrop);
		
		this.add(new PageNavigatorListView());

	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Setters y getters

	public Integer getTotalPages() {
		Long pages = totalRows / pageSize; 
		Long remainder = totalRows % pageSize; 
		if (remainder > 0) {
			pages = pages + 1;
		}
		
		return pages.intValue();
	}
	
	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(Long totalRows) {
		this.totalRows = totalRows;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Metodos utilitarios

	public void setState(Integer currentPage, Long totalRows) {
		if (currentPage == null) {
			this.currentPage = 0;
			this.totalRows = totalRows;
		} else {
			this.currentPage = currentPage;
		}
		
		this.updatePageIndexes();
	}

	public void setState(Integer currentPage, Integer pageSize, Long totalRows) {
		if (currentPage == null) {
			this.currentPage = 0;
			this.totalRows = totalRows;
		} else {
			this.currentPage = currentPage;
		}
		this.pageSize = pageSize;
		
		this.updatePageIndexes();
	}
	
	public void updatePageIndexes() {
		this.pageIndexes.clear();
		
		if (this.totalRows != null && this.pageSize != null && this.totalRows != 0 && this.pageSize != 0) {
			Integer pages = getTotalPages();

			this.pageIndexes.add("PREVIOUS");

			for (int i = 0; i < pages; i++) {
				this.pageIndexes.add(i);
			}

			this.pageIndexes.add("NEXT");
		}
	}
	
	private void updatePredefinedSizes() {
		List<Integer> sizesList = new LinkedList<>();

		try {
			BackofficeProperties properties = ApplicationContextProvider.getBean("backofficeProperties", BackofficeProperties.class);
			String[] sizesArray = properties.getFilasPorPagina().split(",");

			for(String size : sizesArray){
				sizesList.add(Integer.valueOf(size));
			}

			if (sizesList.size() == 0) {
				sizesList.add(5);
				sizesList.add(10);
				sizesList.add(15);
			}
		} catch (Exception ex) {
			sizesList = Arrays.asList(new Integer[]{5,10,50});
		}
		
		PREDEFINED_SIZES = sizesList;
		
		this.pageSize = PREDEFINED_SIZES.get(0); 
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Handlers de los eventos
	
	public abstract void gotoPage(Integer pagina);

    private class PageSizeDropDownChoice extends DropDownChoice<Integer> {
        private static final long serialVersionUID = 1L;

        public PageSizeDropDownChoice() {
            super("pageSizes", new PropertyModel<Integer>(CustomPageNavigator.this, "pageSize"), CustomPageNavigator.this.PREDEFINED_SIZES);
        }

        @Override
        protected boolean wantOnSelectionChangedNotifications() {
            return true;
        }

        @Override
        protected void onSelectionChanged(Integer newSelection) {
            pageSize = newSelection;
            gotoPage(null);
        }

    }

    private class PageNavigatorListView extends ListView<Object> {

        private static final long serialVersionUID = 1L;

        private static final int OFFSET = 3;

        private static final int CANT_PAG_MOSTRAR = OFFSET * 2 + 1;

        private static final int OFFSET_CANT_PAG_PTOS_SUSPENSIVOS = 5;

        public PageNavigatorListView() {
            super("pager", CustomPageNavigator.this.pageIndexes);
        }

        @Override
        protected void populateItem(ListItem<Object> item) {
            Object itemObject = item.getModelObject();

            if (itemObject instanceof String) {
                // Es el indicado de anterior o siguiente
                final String linkType = (String)itemObject;

                if ("PREVIOUS".equals(linkType)) {

                    Link<Void> pageLink = new Link<Void>("pageLink") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick() {
                            if (currentPage > 0) {
                                gotoPage(currentPage - 1);
                            }
                        }
                    };
                    item.add(pageLink);
                    pageLink.setAnchor(CustomPageNavigator.this.anchor);

                    Label pageNumber = new Label("pageNumber", "&laquo;");
                    pageNumber.setEscapeModelStrings(false);
                    pageLink.add(pageNumber);

                    if (currentPage == 0) {
                        item.add(AttributeModifier.append("class", Model.of("disabled")));
                    }
                } else if ("NEXT".equals(linkType)) {

                    final Integer totalPages = getTotalPages();

                    Link<Void> pageLink = new Link<Void>("pageLink") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick() {
                            if (currentPage < totalPages - 1) {
                                gotoPage(currentPage + 1);
                            }
                        }
                    };
                    item.add(pageLink);
                    pageLink.setAnchor(CustomPageNavigator.this.anchor);

                    Label pageNumber = new Label("pageNumber", "&raquo;");
                    pageNumber.setEscapeModelStrings(false);
                    pageLink.add(pageNumber);

                    if (currentPage == totalPages - 1) {
                        item.add(AttributeModifier.append("class", Model.of("disabled")));
                    }
                }

            } else if (itemObject instanceof Integer) {
                // Es un indice de pagina
                final Integer pageIdx = (Integer)itemObject;

                Link<Void> pageLink = new Link<Void>("pageLink") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        gotoPage(pageIdx);
                    }
                };
                item.add(pageLink);
                pageLink.setAnchor(CustomPageNavigator.this.anchor);
                int totalPages = pageIndexes.size() - 2;

                if (totalPages <= CANT_PAG_MOSTRAR) {
                    Label pageNumber = new Label("pageNumber", pageIdx + 1);
                    pageNumber.setEscapeModelStrings(false);
                    pageLink.add(pageNumber);
                } else {
                    int ini = currentPage - OFFSET;
                    int fin = currentPage + OFFSET;

                    if (ini <= 1) {
                        ini = 1;
                        fin = ini + CANT_PAG_MOSTRAR - 1;
                    } else if (fin >= totalPages) {
                        fin = totalPages;
                        ini = fin - CANT_PAG_MOSTRAR + 1;
                    }

                    int posPtosIni = ini - OFFSET_CANT_PAG_PTOS_SUSPENSIVOS <= 1? ini - Math.max((ini - 1) / 2, 1) : ini - OFFSET_CANT_PAG_PTOS_SUSPENSIVOS;
                    int posPtosFin = fin + OFFSET_CANT_PAG_PTOS_SUSPENSIVOS >= totalPages ? fin + Math.max((totalPages - fin) / 2, 1) : fin + OFFSET_CANT_PAG_PTOS_SUSPENSIVOS;

                    Label pageNumber = new Label("pageNumber", (item.getIndex() == posPtosIni || item.getIndex() == posPtosFin)? "..." : pageIdx + 1);
                    pageNumber.setEscapeModelStrings(false);
                    pageLink.add(pageNumber);

                    item.setVisible(item.getIndex() == posPtosIni || item.getIndex() == posPtosFin || (ini <= item.getIndex() && item.getIndex() <= fin));
                }

                if (currentPage.equals(pageIdx)) {
                    item.add(AttributeModifier.append("class", Model.of("active")));
                }
            }
        }
    }
}
