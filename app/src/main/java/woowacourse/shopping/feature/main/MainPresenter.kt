package woowacourse.shopping.feature.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.domain.model.CartProduct
import com.example.domain.model.Product
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.RecentProductRepository
import woowacourse.shopping.feature.main.MainContract.View.MainScreenEvent
import woowacourse.shopping.mapper.toDomain
import woowacourse.shopping.mapper.toPresentation
import woowacourse.shopping.model.CartProductUiModel
import woowacourse.shopping.model.RecentProductUiModel

class MainPresenter(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val recentProductRepository: RecentProductRepository,
) : MainContract.Presenter {

    private val _products: MutableLiveData<List<CartProductUiModel>> = MutableLiveData()
    override val products: LiveData<List<CartProductUiModel>>
        get() = _products

    private val _recentProducts: MutableLiveData<List<RecentProductUiModel>> = MutableLiveData()
    override val recentProducts: LiveData<List<RecentProductUiModel>>
        get() = _recentProducts

    private val _badgeCount: MutableLiveData<Int> = MutableLiveData()
    override val badgeCount: LiveData<Int>
        get() = _badgeCount

    private val _mainScreenEvent: MutableLiveData<MainScreenEvent> =
        MutableLiveData()
    override val mainScreenEvent: LiveData<MainScreenEvent>
        get() = _mainScreenEvent

    override fun initLoadProducts() {
        _mainScreenEvent.value = MainScreenEvent.ShowLoading
        productRepository.fetchFirstProducts(
            onSuccess = { products ->
                loadCartInfo(products)
            },
            onFailure = {
            },
        )
    }

    override fun loadMoreProducts() {
        val lastProductId = _products.value?.lastOrNull()?.productUiModel?.id ?: 0
        productRepository.fetchNextProducts(
            lastProductId,
            onSuccess = { nextProducts ->
                val existProducts =
                    _products.value?.map { it.productUiModel.toDomain() } ?: emptyList()
                loadCartInfo(existProducts + nextProducts)
            },
            onFailure = { _mainScreenEvent.postValue(MainScreenEvent.HideLoadMore) },
        )
    }

    override fun loadRecentProducts() {
        val recentProductUiModels = recentProductRepository.getAll().map { it.toPresentation() }
        _recentProducts.postValue(recentProductUiModels)
    }

    override fun showCartCount() {
        updateCartCountBadge()
    }

    override fun showProductDetail(productId: Long) {
        productRepository.fetchProductById(
            productId,
            onSuccess = { product ->
                val productUiModel = product.toPresentation()
                val recentProduct = _recentProducts.value?.firstOrNull()
                recentProductRepository.addRecentProduct(product)
                _mainScreenEvent.postValue(
                    MainScreenEvent.ShowProductDetailScreen(productUiModel, recentProduct),
                )
            },
            onFailure = {},
        )
    }

    override fun changeProductCartCount(productId: Long, count: Int) {
        val cartProductUiModel: CartProductUiModel =
            _products.value?.find { it.productUiModel.id == productId }
                ?: return
        when {
            cartProductUiModel.cartId <= 0 -> addFirstProductToCart(cartProductUiModel)
            count == 0 -> deleteCartProduct(cartProductUiModel)
            else -> updateCartProductCount(cartProductUiModel, count)
        }
    }

    private fun addFirstProductToCart(cartProductUiModel: CartProductUiModel) {
        val productId = cartProductUiModel.productUiModel.id
        cartRepository.addCartProduct(
            productId,
            onSuccess = { cartId ->
                cartProductUiModel.cartId = cartId
                cartProductUiModel.productUiModel.count = 1
                updateCartCountBadge()
            },
            onFailure = {},
        )
    }

    private fun deleteCartProduct(cartProductUiModel: CartProductUiModel) {
        val cartId = cartProductUiModel.cartId
        cartRepository.deleteCartProduct(
            cartId,
            onSuccess = {
                cartProductUiModel.cartId = -1
                cartProductUiModel.productUiModel.count = 0
                updateCartCountBadge()
            },
            onFailure = {},
        )
    }

    private fun updateCartProductCount(cartProductUiModel: CartProductUiModel, count: Int) {
        val cartId = cartProductUiModel.cartId
        cartRepository.changeCartProductCount(
            cartId,
            count,
            onSuccess = {
                cartProductUiModel.productUiModel.count = count
                updateCartCountBadge()
            },
            onFailure = {},
        )
    }

    private fun loadCartInfo(products: List<Product>) {
        cartRepository.getAll(
            onSuccess = { cartProducts ->
                val cartProductUiModels = createCartProductUiModels(products, cartProducts)
                _products.postValue(cartProductUiModels)
                _mainScreenEvent.postValue(MainScreenEvent.HideLoading)
                updateCartCountBadge()
            },
            onFailure = {},
        )
    }

    private fun updateCartCountBadge() {
        cartRepository.getSize(
            onSuccess = { size -> _badgeCount.postValue(size) },
            onFailure = {},
        )
    }

    private fun createCartProductUiModels(
        products: List<Product>,
        cartInfo: List<CartProduct>,
    ): List<CartProductUiModel> {
        val cartItems = cartInfo.associateBy { it.product.id }
        return products.map { product ->
            val cartItem = cartItems[product.id]
            cartItem?.toPresentation() ?: CartProductUiModel(-1, product.toPresentation(), false)
        }
    }

    override fun moveToCart() {
        _mainScreenEvent.value = MainScreenEvent.ShowCartScreen
    }

    override fun resetProducts() {
        productRepository.resetCache()
    }
}
